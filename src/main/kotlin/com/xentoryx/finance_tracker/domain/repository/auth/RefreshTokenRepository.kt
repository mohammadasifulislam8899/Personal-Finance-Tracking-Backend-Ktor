package com.xentoryx.finance_tracker.domain.repository.auth

import com.xentoryx.finance_tracker.domain.model.RefreshToken
import java.util.UUID

interface RefreshTokenRepository {

    suspend fun save(token: RefreshToken): RefreshToken

    suspend fun findByTokenHash(hash: String): RefreshToken?

    suspend fun findByUserId(userId: UUID): List<RefreshToken>

    suspend fun revokeById(id: UUID): Boolean

    suspend fun revokeAllForUser(userId: UUID): Boolean
}