package com.guireinhardt.FileStorage.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    SecurityFilter securityFilter;
    @Autowired
    CustomAuthenticationEntryPoint customAuthEntryPoint;
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        try {
            return httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/register").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "/h2-console/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/images/**","/css/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/h2-console/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.GET, "/search").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.POST, "/search").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.GET, "/view/**").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.GET, "/storage/**").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.GET, "/download/**").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.GET, "/explorer/view").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.GET, "/explorer/download").hasAnyRole("ADMIN","USERS")
                            .requestMatchers(HttpMethod.POST, "/explorer/download-zip").hasAnyRole("ADMIN","USERS")

                            .requestMatchers("/success", "/error").permitAll()
                            .requestMatchers(HttpMethod.POST, "/storage/delete").hasRole("ADMIN") // Apenas admin pode deletar
                            .requestMatchers(HttpMethod.POST,"/upload/**").hasRole("ADMIN")

                            //requests publicos
                            .requestMatchers(HttpMethod.GET,"/public/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/public/view/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/public/download/**").hasRole("PUBLICO")
                            .requestMatchers(HttpMethod.POST, "/public/download-zip/**").hasRole("PUBLICO")
                            .anyRequest().authenticated()

                    )
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(customAuthEntryPoint) // redirecionamento personalizado
                    )
                    .headers(headers -> headers.frameOptions().sameOrigin())
                    .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/list");
        return successHandler;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
