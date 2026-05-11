package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.RefreshTokenRepository
import com.xentoryx.finance_tracker.security.JwtService
import com.xentoryx.finance_tracker.utils.hashSHA256
import java.time.LocalDateTime

class RefreshTokenUseCase(
    private val tokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService
) {
    suspend operator fun invoke(rawRefreshToken: String): String {

        val token = tokenRepository.findByTokenHash(rawRefreshToken.hashSHA256())
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (token.revokedAt != null)
            throw IllegalArgumentException("Token has been revoked. Please login again.")

        if (token.expiresAt.isBefore(LocalDateTime.now())) {
            tokenRepository.revokeById(token.id)
            throw IllegalArgumentException("Refresh token expired. Please login again.")
        }

        return jwtService.generateAccessToken(token.userId.toString())
    }
}
