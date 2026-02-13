package com.mustafa.smartfoodfitness.config;
// Define a configuration class for setting up CORS (Cross-Origin Resource Sharing) in the application, allowing for cross-origin requests from the specified frontend origin (http://localhost:5173) to enable communication between the frontend and backend during development, while also specifying allowed HTTP methods, headers, and credentials to ensure secure and controlled access to the API endpoints when clients interact with the application from the frontend
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
