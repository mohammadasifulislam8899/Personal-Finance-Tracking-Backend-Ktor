package com.xentoryx.finance_tracker.data.table


import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val currencyCode = varchar("currency_code", 10).default("BDT")
    val isEmailVerified = bool("is_email_verified").default(false)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { java.time.LocalDateTime.now() }
}

object RefreshTokens : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", Users.id)
    val tokenHash = varchar("token_hash", 255).uniqueIndex()
    val deviceInfo = varchar("device_info", 255).nullable()
    val ipAddress = varchar("ip_address", 50).nullable()
    val expiresAt = datetime("expires_at")
    val revokedAt = datetime("revoked_at").nullable()
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
}

object EmailVerifications : UUIDTable("email_verifications") {
    val userId = reference("user_id", Users.id)

    val otp = varchar("otp", 6)

    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()

    val createdAt = datetime("created_at")
}

object PasswordResets : UUIDTable("password_resets") {
    val userId = reference("user_id", Users.id)
    val token = varchar("token", 255).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}