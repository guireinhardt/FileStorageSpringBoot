package com.secretaria.FileStorage.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.exception.FileStorageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e ){
            throw  new FileStorageException("Não foi possivel criar o diretório",e);
        }
    }

    /* public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")){
                throw  new FileStorageException("Desculpe, o nome do arquivo está incorreto " + fileName );
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return  fileName;
        }catch (Exception e){
            throw  new FileStorageException("Não foi possivel salvar o arquivo" + fileName + ". Tente Novamente",e);
        }
    } */
    public String storeFile(MultipartFile file, String folderPath) throws IOException {
        Path destinationFile = this.fileStorageLocation.resolve(Paths.get(folderPath)).resolve(file.getOriginalFilename()).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        return file.getOriginalFilename();
    }


    private String validateFolderPath(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return fileStorageLocation.toString(); // Retorna o diretório base se nenhum caminho for fornecido
        }

        try {
            // Cria um Path a partir do folderPath fornecido
            Path path = Paths.get(folderPath).normalize();

            // Verifica se o caminho está dentro do diretório base
            if (!path.startsWith(fileStorageLocation)) {
                return null; // Caminho inválido
            }

            // Verifica se o diretório existe, se não, cria
            if (!Files.exists(path)) {
                Files.createDirectories(path); // Cria o diretório se não existir
            }

            return path.toString(); // Retorna o caminho validado
        } catch (IOException e) {
            return null; // Retorna null se ocorrer um erro
        }
    }
    /* public Resource loadFileAsResource( String fileName){
        try{
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()){
                return  resource;
            }else {
                throw new FileStorageNotFoundException("File not found " + fileName );
            }

        } catch (Exception e) {
            throw new FileStorageNotFoundException("File not found " + fileName,e );
        }
    } */
    public boolean createFolder(String relativePath) {
        try {
            // Substitui todas as barras invertidas (\) por barras normais (/)
            relativePath = relativePath.replace("\\", "/");

            // Resolve o caminho da pasta e garante que ele seja normalizado
            Path fullPath = fileStorageLocation.resolve(relativePath).normalize();

            // Protege contra ataques de path traversal (evita que o usuário suba pastas com "../")
            if (!fullPath.startsWith(fileStorageLocation)) {
                throw new FileStorageException("Caminho inválido.");
            }

            // Validação do nome da pasta (evita caracteres inválidos para o sistema)
            if (relativePath.matches(".*[<>:\"\\|?*].*")) {
                throw new FileStorageException("O nome da pasta contém caracteres inválidos.");
            }

            // Cria o diretório e seus diretórios intermediários
            File folder = fullPath.toFile();
            if (!folder.exists()) {
                return Files.createDirectories(fullPath) != null;  // Cria a pasta e seus diretórios intermediários
            } else {
                return false;  // A pasta já existe
            }
        } catch (Exception e) {
            throw new FileStorageException("Erro ao criar a pasta '" + relativePath + "': " + e.getMessage(), e);
        }
    }



    public Resource loadFileAsResource(String fileName) {
        try {
            // Resolve o caminho do arquivo a partir do diretório base
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            System.out.println("Tentando carregar o arquivo de: " + filePath.toString()); // Log do caminho
            Resource resource = new UrlResource(filePath.toUri());

            // Verifica se o recurso existe
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageNotFoundException("File not found " + fileName);
            }
        } catch (Exception e) {
            throw new FileStorageNotFoundException("File not found " + fileName, e);
        }
    }
    //método auxiliar para excluir os arquivos recursivamente
    private void deleteDirectoryRecursively(Path path) throws IOException {
        Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // Inverte a ordem para excluir arquivos antes das pastas
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao deletar: " + p);
                    }
                });
    }

    public void moveFileToTrash(String fileName) throws IOException {
        Path sourceFile = fileStorageLocation.resolve(fileName);
        Path trashFile = fileStorageLocation.resolve("lixeira").resolve(fileName);

        if (!Files.exists(sourceFile) || Files.isDirectory(sourceFile)) {
            throw new FileStorageNotFoundException("Arquivo não encontrado: " + fileName);
        }

        Files.createDirectories(trashFile.getParent());
        Files.move(sourceFile, trashFile, StandardCopyOption.REPLACE_EXISTING);
    }


    public void moveFolderToTrash(String folderName) throws IOException {
        Path sourceFolder = fileStorageLocation.resolve(folderName);
        Path trashFolder = fileStorageLocation.resolve("lixeira").resolve(folderName);

        if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
            throw new FileStorageNotFoundException("Pasta não encontrada: " + folderName);
        }

        // Cria diretórios da lixeira, se necessário
        Files.createDirectories(trashFolder.getParent());

        // Move a pasta inteira (inclusive conteúdo)
        Files.walk(sourceFolder)
                .sorted((a, b) -> b.compareTo(a)) // Inverte para mover arquivos antes das pastas
                .forEach(source -> {
                    try {
                        Path destination = trashFolder.resolve(sourceFolder.relativize(source));
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination);
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao mover pasta para a lixeira: " + e.getMessage());
                    }
                });

        // Após mover tudo, deleta a original
        deleteDirectoryRecursively(sourceFolder);
    }
    private Path getTrashFolder() {
        return fileStorageLocation.resolve("lixeira").normalize();  // Local da lixeira
    }

    public void restoreFromTrash(String filePath) throws IOException {

        Path trashFile = getTrashFolder().resolve(filePath);
        Path restoredFile = fileStorageLocation.resolve(filePath);

        if (!Files.exists(trashFile)) {
            throw new FileStorageNotFoundException("Arquivo não encontrado na lixeira: " + filePath);
        }

        Files.createDirectories(restoredFile.getParent());
        Files.move(trashFile, restoredFile, StandardCopyOption.REPLACE_EXISTING);
    }
    // Método para obter pastas na lixeira
    public List<String> getFoldersInTrash() throws IOException {
        Path trashDir = fileStorageLocation.resolve("lixeira"); // Supondo que a lixeira esteja no diretório 'lixeira'
        return Files.walk(trashDir)
                .filter(Files::isDirectory)  // Filtra apenas pastas, ignorando arquivos
                .filter(path -> !path.equals(trashDir))  // Ignora o diretório raiz da lixeira
                .map(path -> trashDir.relativize(path).toString())  // Retorna o caminho relativo da pasta
                .collect(Collectors.toList());
    }

    public void restoreFolderFromTrash(String folderPath) throws IOException {
        Path trashFolder = getTrashFolder().resolve(folderPath);
        Path restoredFolder = fileStorageLocation.resolve(folderPath);

        if (!Files.exists(trashFolder)) {
            throw new FileStorageNotFoundException("Pasta não encontrada na lixeira: " + folderPath);
        }

        Files.createDirectories(restoredFolder);
        Files.walk(trashFolder)
                .sorted((a, b) -> b.compareTo(a)) // Inverte a ordem para restaurar arquivos antes das pastas
                .forEach(source -> {
                    try {
                        Path destination = restoredFolder.resolve(trashFolder.relativize(source));
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination);
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao restaurar da lixeira: " + e.getMessage());
                    }
                });

        deleteDirectoryRecursively(trashFolder);  // Exclui a pasta original da lixeira
    }
    public List<String> getFilesInTrash() throws IOException {
        Path trashFolder = getTrashFolder(); // Obtém o caminho da lixeira
        List<String> filesInTrash = new ArrayList<>();

        // Verifica se a lixeira existe e se não está vazia
        if (Files.exists(trashFolder)) {
            try (Stream<Path> paths = Files.walk(trashFolder)) {
                paths.filter(Files::isRegularFile)  // Filtra apenas os arquivos
                        .forEach(file -> filesInTrash.add(trashFolder.relativize(file).toString())); // Adiciona os arquivos encontrados à lista
            }
        }
        return filesInTrash;
    }
    public void restoreFileFromTrash(String fileName) throws IOException {
        // Caminho do arquivo na lixeira
        Path trashFilePath = fileStorageLocation.resolve("lixeira").resolve(fileName);

        // Verifica se o arquivo existe na lixeira
        if (Files.notExists(trashFilePath)) {
            throw new FileStorageNotFoundException("Arquivo não encontrado na lixeira: " + fileName);
        }

        // Caminho original onde o arquivo deve ser restaurado
        Path originalFilePath = fileStorageLocation.resolve(fileName);

        // Verifica se o arquivo original já existe
        if (Files.exists(originalFilePath)) {
            throw new FileStorageException("Arquivo já existe no diretório original: " + fileName);
        }

        // Restaura o arquivo movendo-o de volta do trash para o local original
        Files.move(trashFilePath, originalFilePath, StandardCopyOption.REPLACE_EXISTING);
    }
    public void deleteFileFromTrash(String filePath) throws IOException {
        Path trashFile = fileStorageLocation.resolve("lixeira").resolve(filePath);
        if (Files.exists(trashFile)) {
            Files.delete(trashFile);
        } else {
            throw new FileStorageNotFoundException("Arquivo não encontrado na lixeira: " + filePath);
        }
    }

    public void deleteFolderFromTrash(String folderPath) throws IOException {
        Path trashFolder = fileStorageLocation.resolve("lixeira").resolve(folderPath);
        if (Files.exists(trashFolder) && Files.isDirectory(trashFolder)) {
            Files.walkFileTree(trashFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            throw new FileStorageNotFoundException("Pasta não encontrada na lixeira: " + folderPath);
        }
    }












}
