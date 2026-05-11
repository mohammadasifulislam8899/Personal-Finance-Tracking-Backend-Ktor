package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.domain.repository.auth.OtpRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.utils.OtpUtils
import java.time.LocalDateTime

class ResendOtpUseCase(
    private val userRepository: UserRepository,
    private val otpRepository: OtpRepository
) {
    suspend operator fun invoke(email: String): Pair<User, String> {

        val user = userRepository.findByEmail(email.trim().lowercase())
            ?: throw IllegalArgumentException("User not found")

        if (user.isEmailVerified)
            throw IllegalArgumentException("Email is already verified")

        otpRepository.invalidateOldOtps(user.id)
        val otp = OtpUtils.generateOtp()
        otpRepository.saveOtp(user.id, otp, LocalDateTime.now().plusMinutes(10))

        return Pair(user, otp)
    }
}