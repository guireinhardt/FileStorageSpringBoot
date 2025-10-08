    package com.secretaria.FileStorage.controller;
    import com.itextpdf.text.Document;
    import com.itextpdf.text.DocumentException;
    import com.itextpdf.text.pdf.PdfPTable;
    import com.itextpdf.text.pdf.PdfWriter;
    import com.itextpdf.text.Paragraph;
    import com.secretaria.FileStorage.entity.*;
    import com.secretaria.FileStorage.infra.security.TokenService;
    import com.secretaria.FileStorage.repository.UsersRepository;
    import jakarta.servlet.http.HttpServletResponse;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.core.io.InputStreamResource;
    import org.springframework.core.io.Resource;
    import org.springframework.http.*;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.BadCredentialsException;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.AuthenticationException;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.userdetails.User;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;

    import java.io.ByteArrayInputStream;
    import java.io.ByteArrayOutputStream;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Controller
    @RequestMapping("auth")
    public class AuthenticationController {
        private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

        @Autowired
        private AuthenticationManager authenticationManager;
        @Autowired
        private UsersRepository repository;
        @Autowired
        private TokenService tokenService;


        // Método para renderizar a página de login
        @GetMapping("/login")
        public String loginPage() {
            return "loginForm"; // Nome do arquivo HTML sem a extensão
        }

        // Login público — só exibe o formulário
        @GetMapping("/login-public")
        public String loginPublic() {
            return "public-login"; // template login-public.html
        }

        // Método para renderizar a página de registro
        @GetMapping("/register")
        public String showRegistrationForm(Model model) {
            return "register"; // Retorna o nome da página de registro (register.html)
        }

        @PostMapping("/login")
        public ResponseEntity<Object> login(@RequestBody AuthenticationDTO data) {
            try {
                logger.debug("Iniciando o processo de login para o usuário: {}", data.login());

                var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
                var auth = this.authenticationManager.authenticate(usernamePassword);

                logger.debug("Usuário autenticado: {}", auth.getName());

                var user = (UsersEntity) auth.getPrincipal();
                var token = tokenService.generateToken(user);

                // Extrai a role
                String role = user.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_PUBLICO");

                logger.debug("Token gerado com sucesso: {}", token);
                logger.debug("Role extraída: {}", role);

                ResponseCookie cookie = ResponseCookie.from("authToken", token)
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(60 * 60) // 1 hora
                        .sameSite("Lax")
                        .build();

                // Retorna a resposta com o token e a role
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(new LoginResponseDTO(token, role));

            } catch (BadCredentialsException e) {
                logger.error("Falha no login: credenciais inválidas para o usuário: {}", data.login());
                // Retorna um LoginErrorResponseDTO com a mensagem de erro
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginErrorResponseDTO("Credenciais inválidas. Por favor, verifique seu usuário e senha."));
            } catch (Exception e) {
                logger.error("Erro inesperado durante o login: {}", e.getMessage());
                // Retorna um LoginErrorResponseDTO com a mensagem de erro genérica
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new LoginErrorResponseDTO("Erro inesperado. Tente novamente."));
            }
        }




        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO data, BindingResult result) {
            System.out.println("Recebido no register: " + data);
            if (result.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                result.getFieldErrors().forEach(fieldError ->
                        errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }

            if (this.repository.findByUsername(data.login()) != null) {
                return ResponseEntity.badRequest().body(Map.of("login", "Login já existe"));
            }

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

            UsersEntity newUser = new UsersEntity(
                    data.nome(),
                    data.cpf(),
                    data.login(),
                    encryptedPassword,
                    data.role()
            );

            this.repository.save(newUser);
            return ResponseEntity.ok().build();
        }


        @GetMapping("/logout")
        public ResponseEntity<Void> logout(HttpServletResponse response) {
            // Invalida o cookie JWT no cliente
            ResponseCookie cookie = ResponseCookie.from("authToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0) // Remove o cookie imediatamente
                    .sameSite("Lax")
                    .build();

            // Adiciona o cookie de logout à resposta
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Redireciona para a página de login (HTTP 302)
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "/auth/login");

            return ResponseEntity.status(HttpStatus.FOUND).build(); // HTTP 302 (Found) para o redirecionamento
        }


        @GetMapping("/account")
        public String accountPage(@CookieValue("authToken") String token, Model model) {
            String login = tokenService.validateToken(token);
            UsersEntity user = (UsersEntity) repository.findByUsername(login);
            model.addAttribute("user", user);
            return "account";
        }

        @PostMapping("/account")
        public String updateAccount(@RequestParam String username,
                                    @RequestParam String password,
                                    @CookieValue("authToken") String token,
                                    Model model) {
            String login = tokenService.validateToken(token);
            UsersEntity user = (UsersEntity) repository.findByUsername(login);

            if (user != null) {
                user.setUsername(username);
                user.setPassword(new BCryptPasswordEncoder().encode(password));
                repository.save(user);
                model.addAttribute("success", true);
            }

            return "account"; // Recarrega a mesma página com os dados atualizados
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/admin/users")
        public String listUsers(Model model) {
            // Calcular total de usuários, ativos e inativos
            long totalUsers = repository.count();  // Conta o total de usuários
            long activeUsersCount = repository.countActiveUsers();  // Conta os usuários ativos
            long inactiveUsersCount = totalUsers - activeUsersCount;  // Calcula os inativos


            // Buscar a lista de usuários
            model.addAttribute("users", repository.findAll());

            // Adicionar os valores ao modelo para exibir no template
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("activeUsersCount", activeUsersCount);
            model.addAttribute("inactiveUsersCount", inactiveUsersCount);

            return "admin/admin-users";  // Retorna o template da administração de usuários
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin/users/{id}/change-password")
        public String changeUserPassword(@PathVariable Long id, @RequestParam("newPassword") String newPassword) {
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
            repository.save(user);
            return "redirect:/admin/users?success";
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin/users/{id}/change-role")
        public String changeUserRole(@PathVariable Long id, @RequestParam("newRole") UsersRole newRole) {
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            user.setRole(newRole);
            repository.save(user);
            return "redirect:/admin/users?success";
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin/users/{id}/delete")
        public String deleteUser(@PathVariable Long id) {
            repository.deleteById(id);
            return "redirect:/admin/users?deleted";
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin/users/{id}/toggle-status")
        public String toggleUserStatus(@PathVariable Long id) {
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            user.setEnabled(!user.isEnabled());
            repository.save(user);
            return "redirect:/admin/users?statusChanged";
        }
        /*
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/auth/account/{id}")
        public String editAccount(@PathVariable Long id, Model model) {
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            model.addAttribute("user", user);
            return "account/edit-account";
        } */

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/auth/account/{id}")
        public String updateAccount(@PathVariable Long id,
                                    @RequestParam String username,
                                    @RequestParam(required = false) String password,
                                    Model model) {
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            user.setUsername(username);

            if (password != null && !password.isEmpty()) {
                user.setPassword(new BCryptPasswordEncoder().encode(password));
            }

            repository.save(user);
            model.addAttribute("user", user);
            model.addAttribute("success", true);
            return "account/edit-account";
        }
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/admin/users/{id}/details-edit")
        public String viewAndEditUserDetails(@PathVariable Long id, Model model) {
            // Busca o usuário pelo ID
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Adiciona o usuário ao modelo para exibição e edição na mesma página
            model.addAttribute("user", user);

            // Lista de roles para o select (caso o administrador queira alterar a role)
            model.addAttribute("roles", Arrays.asList("ADMIN", "USERS", "PUBLICO"));

            return "admin/user-details"; // Página que exibe os detalhes e permite a edição
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin/users/{id}/update-role")
        public String updateUserRole(@PathVariable Long id, @RequestParam("newRole") String newRole) {
            // Busca o usuário pelo ID
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Altera a role do usuário
            user.setRole(UsersRole.valueOf(newRole)); // Atualiza a role
            repository.save(user);

            // Redireciona de volta para a página de detalhes com uma mensagem de sucesso
            return "redirect:/auth/admin/users/" + id + "/details?roleUpdated"; // Parâmetro de sucesso (opcional)
        }
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin/users/{id}/update")
        public String updateUser(@PathVariable Long id,
                                 @RequestParam String role,
                                 @RequestParam(required = false) Boolean enabled,
                                 Model model) {

            // Valida se o "role" ou "status" não foi selecionado corretamente
            if (role == null || role.isEmpty()) {
                model.addAttribute("error", "A Role deve ser selecionada.");
                return "redirect:/auth/admin/users/" + id + "/details-edit";
            }

            if (enabled == null) {
                model.addAttribute("error", "O Status deve ser selecionado.");
                return "redirect:/auth/admin/users/" + id + "/details-edit";
            }

            // Busca o usuário pelo ID
            UsersEntity user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Atualiza a role e o status do usuário
            user.setRole(UsersRole.valueOf(role));  // Atualiza a role
            user.setEnabled(enabled);                // Atualiza o status

            // Salva as alterações no banco de dados
            repository.save(user);

            return "redirect:/auth/admin/users/" + id + "/details-edit?success=true"; // Redireciona após sucesso
        }

        // Exportando os arquivos
        // Exportando os arquivos
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/admin/export/{format}")
        public ResponseEntity<Resource> exportData(@RequestParam(required = false) String role,
                                                   @RequestParam(required = false) String status,
                                                   @PathVariable String format,
                                                   Model model) throws DocumentException {

            // Ajustar os valores de role e status
            if (role == null || role.equals("all")) {
                role = "all";  // Se role for null ou 'all', aplica todos os usuários
            }

            if (status == null || status.equals("all")) {
                status = null;  // Se status for 'all' ou null, busca todos os status
            }

            // Exemplo genérico de lista de usuários, você pode passar qualquer lista aqui
            List<UsersEntity> users = repository.findAll();  // Obter todos os usuários (ou com filtros)

            // Filtros aplicados
            if (!role.equals("all")) {
                users = repository.findByRole(role); // Filtro por role
            }
            if (status != null) {
                boolean isActive = Boolean.parseBoolean(status);
                users = repository.findByEnabled(isActive); // Filtro por status
            }

            if ("csv".equalsIgnoreCase(format)) {
                return exportToCSV(users);  // Exportar para CSV
            } else if ("pdf".equalsIgnoreCase(format)) {
                return exportToPDF(users);  // Exportar para PDF
            } else {
                return ResponseEntity.badRequest().build();  // Caso o formato não seja reconhecido
            }
        }


        private ResponseEntity<Resource> exportToCSV(List<UsersEntity> users) {
            // Criar conteúdo CSV
            String csvContent = "Nome,Username,Role,Status\n";
            for (UsersEntity user : users) {
                csvContent += user.getNome() + "," + user.getUsername() + "," + user.getRole() + "," + (user.isEnabled() ? "Ativo" : "Desabilitado") + "\n";
            }

            // Criar o arquivo CSV
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csvContent.getBytes()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        }

        public ResponseEntity<Resource> exportToPDF(List<UsersEntity> users) throws DocumentException {
            // Criação do documento PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(); // Instanciando o objeto Document
            PdfWriter.getInstance(document, outputStream); // Associa o documento ao writer que escreve no ByteArrayOutputStream
            document.open(); // Abre o documento para começar a adicionar conteúdo

            // Adiciona título no PDF
            document.add(new Paragraph("Usuários:"));
            document.add(new Paragraph("\n"));

            // Criação de uma tabela com 4 colunas (Nome, Username, Role, Status)
            PdfPTable table = new PdfPTable(4); // 4 colunas para os dados
            table.setWidthPercentage(100); // Define que a tabela vai ocupar 100% da largura da página

            // Adiciona cabeçalho da tabela
            table.addCell("Nome");
            table.addCell("Username");
            table.addCell("Role");
            table.addCell("Status");

            // Adiciona os dados dos usuários à tabela
            for (UsersEntity user : users) {
                table.addCell(user.getNome()); // Nome
                table.addCell(user.getUsername()); // Username

                // Aqui, convertendo o enum para String com .toString()
                table.addCell(user.getRole().toString()); // Role, usando .toString() para converter enum em String

                table.addCell(user.isEnabled() ? "Ativo" : "Desabilitado"); // Status
            }

            // Adiciona a tabela no documento
            document.add(table);

            document.close(); // Fecha o documento após adicionar todo o conteúdo

            // Cria o InputStreamResource a partir do ByteArrayOutputStream
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray()));

            // Retorna o arquivo PDF como resposta
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        }

    }



