package com.xentoryx.finance_tracker.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val currencyCode: String,
    val isEmailVerified: Boolean,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val deviceInfo: String?,
    val ipAddress: String?,
    val expiresAt: LocalDateTime,
    val revokedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class EmailVerification(
    val id: UUID,
    val userId: UUID,
    val otp: String,
    val expiresAt: LocalDateTime,
    val usedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class PasswordReset(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: LocalDateTime,
    val usedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)