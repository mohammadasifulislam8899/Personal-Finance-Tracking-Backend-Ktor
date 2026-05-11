package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable data class MessageResponse(val message: String)
@Serializable data class TokenResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val currencyCode: String,
    val isEmailVerified: Boolean,
    val isActive: Boolean
)

@Serializable
data class AuthResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RegisterResponse(
    val user: UserResponse,
    val message: String
)
