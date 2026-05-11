package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.model.PasswordReset
import com.xentoryx.finance_tracker.domain.repository.auth.PasswordResetRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.utils.hashSHA256
import java.time.LocalDateTime
import java.util.UUID

class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
    private val passwordResetRepository: PasswordResetRepository
) {
    suspend operator fun invoke(email: String): Pair<String, String> {

        val user = userRepository.findByEmail(email.trim().lowercase())
            ?: throw IllegalArgumentException("If this email exists, a reset link will be sent")

        val rawToken = UUID.randomUUID().toString()

        passwordResetRepository.create(
            PasswordReset(
                id = UUID.randomUUID(),
                userId = user.id,
                token = rawToken.hashSHA256(),
                expiresAt = LocalDateTime.now().plusHours(1),
                usedAt = null,
                createdAt = LocalDateTime.now()
            )
        )

        return Pair(rawToken, user.email)
    }
}