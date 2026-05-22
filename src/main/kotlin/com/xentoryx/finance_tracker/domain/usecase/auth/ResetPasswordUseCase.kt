package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.PasswordResetRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.utils.PasswordUtils
import com.xentoryx.finance_tracker.utils.hashSHA256
import java.time.LocalDateTime

class ResetPasswordUseCase(
    private val passwordResetRepository: PasswordResetRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(rawToken: String, newPassword: String) {

        if (newPassword.length < 8)
            throw ValidationException("Password must be at least 8 characters")

        // FIX: removed redundant expiry check — findValidToken() already filters expired tokens
        val resetEntry = passwordResetRepository.findValidToken(rawToken.hashSHA256())
            ?: throw ValidationException("Invalid or expired reset token")

        val user = userRepository.findById(resetEntry.userId)
            ?: throw NotFoundException("User not found")

        userRepository.update(
            user.copy(
                passwordHash = PasswordUtils.hashPassword(newPassword),
                updatedAt    = LocalDateTime.now()
            )
        )
        passwordResetRepository.markUsed(resetEntry.id)
    }
}
