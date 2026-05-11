package com.xentoryx.finance_tracker.domain.repository.auth

import com.xentoryx.finance_tracker.domain.model.User
import java.util.UUID

interface UserRepository {

    suspend fun create(user: User): User

    suspend fun findById(id: UUID): User?

    suspend fun findByEmail(email: String): User?

    suspend fun update(user: User): User

    suspend fun delete(id: UUID): Boolean
}