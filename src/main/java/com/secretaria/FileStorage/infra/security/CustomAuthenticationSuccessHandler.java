package com.secretaria.FileStorage.infra.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/";  // fallback

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                //alterar as rotas de direcionamento
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_USERS")) {
                redirectUrl = "/users/home";
                break;
            } else if (role.equals("ROLE_PUBLICO")) {

                redirectUrl = "/public/public-files";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
