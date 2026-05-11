package com.xentoryx.finance_tracker.data.mapper

import com.xentoryx.finance_tracker.data.table.*
import com.xentoryx.finance_tracker.domain.model.*
import org.jetbrains.exposed.v1.core.ResultRow

// ==========================
// User Mapper
// ==========================

fun ResultRow.toUser(): User {
    return User(
        id = this[Users.id].value,
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        fullName = this[Users.fullName],
        currencyCode = this[Users.currencyCode],
        isEmailVerified = this[Users.isEmailVerified],
        isActive = this[Users.isActive],
        createdAt = this[Users.createdAt],
        updatedAt = this[Users.updatedAt]
    )
}

// ==========================
// RefreshToken Mapper
// ==========================

fun ResultRow.toRefreshToken(): RefreshToken {
    return RefreshToken(
        id = this[RefreshTokens.id].value,
        userId = this[RefreshTokens.userId].value,
        tokenHash = this[RefreshTokens.tokenHash],
        deviceInfo = this[RefreshTokens.deviceInfo],
        ipAddress = this[RefreshTokens.ipAddress],
        expiresAt = this[RefreshTokens.expiresAt],
        revokedAt = this[RefreshTokens.revokedAt],
        createdAt = this[RefreshTokens.createdAt]
    )
}

// ==========================
// EmailVerification Mapper
// ==========================

fun ResultRow.toEmailVerification(): EmailVerification {
    return EmailVerification(
        id = this[EmailVerifications.id].value,
        userId = this[EmailVerifications.userId].value,
        otp = this[EmailVerifications.otp],
        expiresAt = this[EmailVerifications.expiresAt],
        usedAt = this[EmailVerifications.usedAt],
        createdAt = this[EmailVerifications.createdAt]
    )
}

// ==========================
// PasswordReset Mapper
// ==========================

fun ResultRow.toPasswordReset(): PasswordReset {
    return PasswordReset(
        id = this[PasswordResets.id].value,
        userId = this[PasswordResets.userId].value,
        token = this[PasswordResets.token],
        expiresAt = this[PasswordResets.expiresAt],
        usedAt = this[PasswordResets.usedAt],
        createdAt = this[PasswordResets.createdAt]
    )
}


// ==========================
// List Helpers (optional but useful)
// ==========================

fun Iterable<ResultRow>.toUsers(): List<User> = map { it.toUser() }

fun Iterable<ResultRow>.toRefreshTokens(): List<RefreshToken> = map { it.toRefreshToken() }