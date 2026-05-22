package com.xentoryx.finance_tracker.domain.usecase.auth

import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw NotFoundException("User not found")
    }
}
