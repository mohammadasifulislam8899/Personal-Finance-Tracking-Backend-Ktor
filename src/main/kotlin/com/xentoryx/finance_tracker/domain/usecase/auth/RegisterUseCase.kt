package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.domain.repository.auth.OtpRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.exception.ConflictException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.utils.OtpUtils
import com.xentoryx.finance_tracker.utils.PasswordUtils
import java.time.LocalDateTime
import java.util.UUID

class RegisterUseCase(
    private val userRepository: UserRepository,
    private val otpRepository: OtpRepository
) {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(email: String, password: String, fullName: String): Pair<User, String> {

        if (!email.trim().matches(emailRegex))
            throw ValidationException("Invalid email format")

        if (password.length < 8)
            throw ValidationException("Password must be at least 8 characters")

        if (fullName.isBlank())
            throw ValidationException("Full name cannot be empty")

        if (userRepository.findByEmail(email.trim().lowercase()) != null)
            throw ConflictException("User already exists with this email")

        val user = User(
            id              = UUID.randomUUID(),
            email           = email.trim().lowercase(),
            passwordHash    = PasswordUtils.hashPassword(password),
            fullName        = fullName.trim(),
            currencyCode    = "BDT",
            isEmailVerified = false,
            isActive        = true,
            createdAt       = LocalDateTime.now(),
            updatedAt       = LocalDateTime.now()
        )

        val savedUser = userRepository.create(user)
        otpRepository.invalidateOldOtps(savedUser.id)
        val otp = OtpUtils.generateOtp()
        otpRepository.saveOtp(savedUser.id, otp, LocalDateTime.now().plusMinutes(10))

        return Pair(savedUser, otp)
    }
}
