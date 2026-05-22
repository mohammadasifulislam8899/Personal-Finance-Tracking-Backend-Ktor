package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.repository.auth.OtpRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import java.time.LocalDateTime
import java.util.UUID

class VerifyEmailUseCase(
    private val otpRepository: OtpRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UUID, otp: String) {

        val user = userRepository.findById(userId)
            ?: throw NotFoundException("User not found")

        if (user.isEmailVerified)
            throw ValidationException("Email is already verified")

        val isValid = otpRepository.findValidOtp(userId, otp)
        if (!isValid) throw ValidationException("Invalid or expired OTP")

        otpRepository.markAsUsed(userId, otp)
        userRepository.update(user.copy(isEmailVerified = true, updatedAt = LocalDateTime.now()))
    }
}
