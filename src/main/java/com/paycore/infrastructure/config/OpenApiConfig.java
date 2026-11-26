package com.paycore.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình tài liệu OpenAPI 3 / Swagger UI và khai báo Bearer JWT Security Scheme.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Zefra PayCore API",
                version = "v1.0.0",
                description = "Core Payment, Double-Entry Ledger và Fraud Evaluation API cho hệ thống PayCore.",
                contact = @Contact(name = "PayCore Engineering", email = "engineering@paycore.local"),
                license = @License(name = "Proprietary")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Nhập JWT token lấy từ endpoint POST /api/v1/auth/login"
)
public class OpenApiConfig {
}
