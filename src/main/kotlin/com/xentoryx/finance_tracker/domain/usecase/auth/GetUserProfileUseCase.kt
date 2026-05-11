package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import java.util.UUID

class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")
    }
}