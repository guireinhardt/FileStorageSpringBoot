package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.entity.*;
import com.secretaria.FileStorage.infra.security.TokenService;
import com.secretaria.FileStorage.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
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

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponseDTO(token, role));
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
    public ResponseEntity<Void> logout() {
        // Invalida o cookie JWT no cliente
        ResponseCookie cookie = ResponseCookie.from("authToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Remove o cookie imediatamente
                .sameSite("Lax")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
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
        model.addAttribute("users", repository.findAll());
        return "admin/users";
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











}



