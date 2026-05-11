package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.utils.PasswordUtils

class LoginUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): User {

        val user = userRepository.findByEmail(email.trim().lowercase())
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!PasswordUtils.checkPassword(password, user.passwordHash))
            throw IllegalArgumentException("Invalid email or password")

        if (!user.isActive)
            throw IllegalArgumentException("Account is deactivated")

        if (!user.isEmailVerified)
            throw IllegalArgumentException("Please verify your email before logging in")

        return user
    }
}