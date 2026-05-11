package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.RefreshTokenRepository
import java.util.UUID

class LogoutUseCase(
    private val tokenRepository: RefreshTokenRepository
) {
    suspend operator fun invoke(userId: UUID) {
        tokenRepository.revokeAllForUser(userId)
    }
}