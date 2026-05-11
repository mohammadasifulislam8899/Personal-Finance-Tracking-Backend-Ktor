package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.OtpRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import java.time.LocalDateTime
import java.util.UUID

class VerifyEmailUseCase(
    private val otpRepository: OtpRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UUID, otp: String) {

        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        if (user.isEmailVerified)
            throw IllegalArgumentException("Email is already verified")

        val isValid = otpRepository.findValidOtp(userId, otp)
        if (!isValid) throw IllegalArgumentException("Invalid or expired OTP")

        otpRepository.markAsUsed(userId, otp)

        userRepository.update(user.copy(isEmailVerified = true, updatedAt = LocalDateTime.now()))
    }
}