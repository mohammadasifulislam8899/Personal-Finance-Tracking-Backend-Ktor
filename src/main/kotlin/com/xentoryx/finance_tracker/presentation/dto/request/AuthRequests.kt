package com.xentoryx.finance_tracker.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable data class RegisterRequest(val email: String, val password: String, val fullName: String)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class VerifyEmailRequest(val userId: String, val otp: String)
@Serializable data class ResendOtpRequest(val email: String)
@Serializable data class RefreshTokenRequest(val refreshToken: String)
@Serializable data class ForgotPasswordRequest(val email: String)
@Serializable data class ResetPasswordRequest(val token: String, val newPassword: String)
