package com.interviewprep.controller;

import com.interviewprep.dto.ApiResponse;
import com.interviewprep.dto.AuthRequest;
import com.interviewprep.dto.AuthResponse;
import com.interviewprep.dto.ChangePasswordRequest;
import com.interviewprep.dto.RegisterRequest;
import com.interviewprep.dto.UserDto;
import com.interviewprep.dto.VerifyOtpRequest;
import com.interviewprep.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and profile")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password to receive JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify email OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody VerifyOtpRequest req) {
        AuthResponse response = authService.verifyOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully", response));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP email")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        UserDto profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password by verifying old password and replacing with new password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }
}
