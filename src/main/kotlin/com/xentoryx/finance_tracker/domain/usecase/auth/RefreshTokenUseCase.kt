package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.RefreshTokenRepository
import com.xentoryx.finance_tracker.exception.AuthenticationException
import com.xentoryx.finance_tracker.security.JwtService
import com.xentoryx.finance_tracker.utils.hashSHA256
import java.time.LocalDateTime

class RefreshTokenUseCase(
    private val tokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService
) {
    suspend operator fun invoke(rawRefreshToken: String): String {

        val token = tokenRepository.findByTokenHash(rawRefreshToken.hashSHA256())
            ?: throw AuthenticationException("Invalid refresh token")

        if (token.revokedAt != null)
            throw AuthenticationException("Token has been revoked. Please login again.")

        if (token.expiresAt.isBefore(LocalDateTime.now())) {
            tokenRepository.revokeById(token.id)
            throw AuthenticationException("Refresh token expired. Please login again.")
        }

        return jwtService.generateAccessToken(token.userId.toString())
    }
}
