package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.PasswordResetRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.utils.PasswordUtils
import com.xentoryx.finance_tracker.utils.hashSHA256
import java.time.LocalDateTime

class ResetPasswordUseCase(
    private val passwordResetRepository: PasswordResetRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(rawToken: String, newPassword: String) {

        if (newPassword.length < 8)
            throw IllegalArgumentException("Password must be at least 8 characters")

        val resetEntry = passwordResetRepository.findValidToken(rawToken.hashSHA256())
            ?: throw IllegalArgumentException("Invalid or expired reset token")

        if (resetEntry.expiresAt.isBefore(LocalDateTime.now()))
            throw IllegalArgumentException("Reset token has expired")

        val user = userRepository.findById(resetEntry.userId)
            ?: throw IllegalArgumentException("User not found")

        userRepository.update(user.copy(passwordHash = PasswordUtils.hashPassword(newPassword), updatedAt = LocalDateTime.now()))
        passwordResetRepository.markUsed(resetEntry.id)
    }
}