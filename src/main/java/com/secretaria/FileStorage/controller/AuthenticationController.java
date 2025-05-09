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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

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

    // Método para renderizar a página de registro
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register"; // Retorna o nome da página de registro (register.html)
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody  AuthenticationDTO data) {
        logger.debug("Iniciando o processo de login para o usuário: {}", data.login());

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        logger.debug("Usuário autenticado: {}", auth.getName());

        var token = tokenService.generateToken((UsersEntity ) auth.getPrincipal());
        logger.debug("Token gerado com sucesso: {}", token);
        ResponseCookie cookie = ResponseCookie.from("authToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60) //1 hora
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Adiciona o cookie ao cabeçalho
                .body(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Validated RegisterDTO data) {
        if (this.repository.findByUsername(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        UsersEntity newUser = new UsersEntity(data.login(),
                encryptedPassword,
                data.role());

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

}



