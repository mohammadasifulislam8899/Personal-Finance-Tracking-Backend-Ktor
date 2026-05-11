package com.xentoryx.finance_tracker.domain.repository.auth

import java.time.LocalDateTime
import java.util.UUID

interface OtpRepository {
    suspend fun saveOtp(userId: UUID, otp: String, expiresAt: LocalDateTime)
    suspend fun invalidateOldOtps(userId: UUID)
    suspend fun findValidOtp(userId: UUID, otp: String): Boolean
    suspend fun markAsUsed(userId: UUID, otp: String)
}