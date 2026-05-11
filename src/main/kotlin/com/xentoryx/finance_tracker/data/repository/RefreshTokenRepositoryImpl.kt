package com.xentoryx.finance_tracker.data.repository

import com.xentoryx.finance_tracker.data.mapper.toRefreshToken
import com.xentoryx.finance_tracker.data.table.RefreshTokens
import com.xentoryx.finance_tracker.domain.model.RefreshToken
import com.xentoryx.finance_tracker.domain.repository.auth.RefreshTokenRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.UUID

class RefreshTokenRepositoryImpl(
    private val db: R2dbcDatabase
) : RefreshTokenRepository {

    override suspend fun save(token: RefreshToken): RefreshToken {
        return suspendTransaction(db) {
            RefreshTokens.insert {
                it[userId] = token.userId
                it[tokenHash] = token.tokenHash
                it[deviceInfo] = token.deviceInfo
                it[ipAddress] = token.ipAddress
                it[expiresAt] = token.expiresAt
                it[revokedAt] = token.revokedAt
                it[createdAt] = token.createdAt
            }
            token
        }
    }

    override suspend fun findByTokenHash(hash: String): RefreshToken? {
        return suspendTransaction(db) {
            RefreshTokens.selectAll()
                .where { RefreshTokens.tokenHash eq hash }
                .map { it.toRefreshToken() }
                .singleOrNull()
        }
    }

    override suspend fun findByUserId(userId: UUID): List<RefreshToken> {
        return suspendTransaction(db) {
            RefreshTokens.selectAll()
                .where { RefreshTokens.userId eq userId }
                .map { it.toRefreshToken() }
                .toList()
        }
    }

    override suspend fun revokeById(id: UUID): Boolean {
        return suspendTransaction(db) {
            RefreshTokens.deleteWhere { RefreshTokens.id eq id } > 0
        }
    }

    override suspend fun revokeAllForUser(userId: UUID): Boolean {
        return suspendTransaction(db) {
            RefreshTokens.deleteWhere { RefreshTokens.userId eq userId } > 0
        }
    }
}
