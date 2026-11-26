package com.paycore.api.controller;

import com.paycore.api.dto.ApiResponse;
import com.paycore.api.dto.AuthResponse;
import com.paycore.api.dto.ErrorResponse;
import com.paycore.api.dto.LoginRequest;
import com.paycore.domain.enums.UserRole;
import com.paycore.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API xác thực và cấp phát JWT Access Token")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Xác thực thông tin tài khoản và trả về JWT Bearer token để truy cập các API bảo vệ."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Sai tên đăng nhập hoặc mật khẩu",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Boilerplate cấp token mẫu khớp với seed user (thay bằng UserDetailsService + PasswordEncoder khi nối DB)
        UUID demoUserId = UUID.fromString("10000000-0000-0000-0000-000000000004");
        UserRole demoRole = UserRole.USER;
        String token = jwtTokenProvider.generateToken(demoUserId, request.username(), demoRole);

        AuthResponse authResponse = new AuthResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationMs(),
                request.username(),
                demoRole
        );
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", authResponse));
    }
}
