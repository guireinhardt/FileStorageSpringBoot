package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.entity.AuthenticationDTO;
import com.secretaria.FileStorage.entity.LoginResponseDTO;
import com.secretaria.FileStorage.entity.RegisterDTO;
import com.secretaria.FileStorage.entity.UsersEntity;
import com.secretaria.FileStorage.infra.security.TokenService;
import com.secretaria.FileStorage.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("auth")
public class AuthenticationController {

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
    public String registerPage() {
        return "register"; // Nome do arquivo HTML sem a extensão
    }
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Validated AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((UsersEntity) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
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
}



