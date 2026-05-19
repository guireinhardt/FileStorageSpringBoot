package com.guireinhardt.FileStorage.service;

import com.guireinhardt.FileStorage.entity.KeywordEntity;
import com.guireinhardt.FileStorage.entity.SubkeywordEntity;
import com.guireinhardt.FileStorage.repository.KeywordRepository;
import com.guireinhardt.FileStorage.repository.SubkeywordRepository;
import com.guireinhardt.FileStorage.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final SubkeywordRepository subkeywordRepository;

    // Construtor
    public KeywordService(KeywordRepository keywordRepository, SubkeywordRepository subkeywordRepository) {
        this.keywordRepository = keywordRepository;
        this.subkeywordRepository = subkeywordRepository;
    }

    // Método que será executado ao iniciar a aplicação
    @PostConstruct
    public void init() {
        // Buscar todas as palavras-chave do banco de dados
        List<KeywordEntity> keywords = keywordRepository.findAll();

        // Itera sobre as palavras-chave e suas subpalavras
        for (KeywordEntity keyword : keywords) {
            // Exemplo de exibição de palavras-chave e subpalavras no log
            System.out.println("Palavra-chave: " + keyword.getPalavra());

            // Buscar as subpalavras relacionadas a essa palavra-chave
            List<SubkeywordEntity> subKeywords = subkeywordRepository.findByKeyword(keyword);
            for (SubkeywordEntity sub : subKeywords) {
                System.out.println("  Subkeyword: " + sub.getPalavra());
            }
        }
    }
    public List<KeywordEntity> getAllKeywords() {
        return keywordRepository.findAll();
    }

    // Método para adicionar novas palavras-chave e subpalavras
    public void addKeyword(String keyword, List<String> subKeywords) {
        // Cria uma nova KeywordEntity
        KeywordEntity newKeyword = new KeywordEntity();
        newKeyword.setPalavra(keyword);
        KeywordEntity savedKeyword = keywordRepository.save(newKeyword);

        // Adiciona as subpalavras associadas
        for (String sub : subKeywords) {
            SubkeywordEntity subkeyword = new SubkeywordEntity();
            subkeyword.setPalavra(sub);
            subkeyword.setKeyword(savedKeyword);  // Associa a subpalavra com a palavra-chave
            subkeywordRepository.save(subkeyword);
        }
    }

    // Método para adicionar uma subpalavra a uma palavra-chave existente
    public void addSubKeyword(String keyword, String subKeyword) {
        // Encontra a KeywordEntity existente no banco de dados pela palavra (não pelo ID)
        KeywordEntity existingKeyword = keywordRepository.findByPalavra(keyword);

        // Verifica se a palavra-chave foi encontrada. Se não, lança uma exceção.
        if (existingKeyword == null) {
            throw new RuntimeException("Keyword não encontrada");
        }

        // Cria uma nova Subkeyword associada à KeywordEntity
        SubkeywordEntity newSubKeyword = new SubkeywordEntity();
        newSubKeyword.setPalavra(subKeyword);
        newSubKeyword.setKeyword(existingKeyword); // Associa a subpalavra com a palavra-chave
        subkeywordRepository.save(newSubKeyword);
    }



    // Método de pesquisa que normaliza a string de pesquisa e faz a busca sem acento
    public List<KeywordEntity> searchKeywords(String searchTerm) {
        // Normaliza a string de pesquisa (sem acentos)
        String normalizedSearchTerm = StringUtils.normalize(searchTerm);

        // Buscar todas as palavras-chave do banco de dados
        List<KeywordEntity> keywords = keywordRepository.findAll();

        // Filtra as palavras-chave que correspondem ao termo de pesquisa sem acento
        return keywords.stream()
                .filter(keyword -> StringUtils.normalize(keyword.getPalavra()).contains(normalizedSearchTerm))
                .collect(Collectors.toList());
    }

    // Método de pesquisa para subpalavras, também normalizando a string de pesquisa
    public List<SubkeywordEntity> searchSubkeywords(String searchTerm) {
        // Normaliza a string de pesquisa (sem acentos)
        String normalizedSearchTerm = StringUtils.normalize(searchTerm);

        // Buscar todas as subpalavras do banco de dados
        List<SubkeywordEntity> subkeywords = subkeywordRepository.findAll();

        // Filtra as subpalavras que correspondem ao termo de pesquisa sem acento
        return subkeywords.stream()
                .filter(sub -> StringUtils.normalize(sub.getPalavra()).contains(normalizedSearchTerm))
                .collect(Collectors.toList());
    }

    // Método para pesquisar tanto palavras-chave quanto subpalavras
    public List<KeywordEntity> searchKeywordsAndSubkeywords(String searchTerm) {
        // Normaliza a string de pesquisa (sem acentos)
        String normalizedSearchTerm = StringUtils.normalize(searchTerm);

        // Buscar todas as palavras-chave do banco de dados
        List<KeywordEntity> keywords = keywordRepository.findAll();

        // Filtra as palavras-chave que correspondem ao termo de pesquisa normalizado
        List<KeywordEntity> matchedKeywords = keywords.stream()
                .filter(keyword -> StringUtils.normalize(keyword.getPalavra()).contains(normalizedSearchTerm))
                .collect(Collectors.toList());

        // Buscar todas as subpalavras do banco de dados
        List<SubkeywordEntity> subkeywords = subkeywordRepository.findAll();

        // Filtra subpalavras que correspondem ao termo de pesquisa normalizado
        List<SubkeywordEntity> matchedSubkeywords = subkeywords.stream()
                .filter(sub -> StringUtils.normalize(sub.getPalavra()).contains(normalizedSearchTerm))
                .collect(Collectors.toList());

        // Aqui você pode retornar tanto keywords quanto subkeywords conforme necessário
        return matchedKeywords; // ou matchedSubkeywords
    }
}