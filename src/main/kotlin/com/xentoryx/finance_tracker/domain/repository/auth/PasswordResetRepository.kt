package com.xentoryx.finance_tracker.domain.repository.auth

import com.xentoryx.finance_tracker.domain.model.PasswordReset
import java.util.UUID

interface PasswordResetRepository {

    suspend fun create(entry: PasswordReset): PasswordReset

    suspend fun findValidToken(token: String): PasswordReset?

    suspend fun markUsed(id: UUID): Boolean
}