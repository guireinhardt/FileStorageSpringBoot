package com.secretaria.FileStorage.config;

import com.secretaria.FileStorage.interceptor.SearchLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SearchLoggingInterceptor searchLoggingInterceptor;

    // Injeta o SearchLoggingInterceptor
    public WebConfig(SearchLoggingInterceptor searchLoggingInterceptor) {
        this.searchLoggingInterceptor = searchLoggingInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite todas as rotas
                .allowedOrigins("*") // Permite todas as origens (substitua por origens específicas em produção)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*"); // Permite todos os cabeçalhos
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registra o SearchLoggingInterceptor nas rotas de busca
        registry.addInterceptor(searchLoggingInterceptor)
                .addPathPatterns("/search", "/banco-de-imagens/search"); // Ajuste conforme necessário
    }
}
