package com.xentoryx.finance_tracker.data.repository.auth

import com.xentoryx.finance_tracker.data.mapper.toPasswordReset
import com.xentoryx.finance_tracker.data.table.PasswordResets
import com.xentoryx.finance_tracker.domain.model.PasswordReset
import com.xentoryx.finance_tracker.domain.repository.auth.PasswordResetRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.time.LocalDateTime
import java.util.UUID

class PasswordResetRepositoryImpl(
    private val db: R2dbcDatabase
) : PasswordResetRepository {

    override suspend fun create(entry: PasswordReset): PasswordReset {
        return suspendTransaction(db) {
            PasswordResets.insert {
                it[userId] = entry.userId
                it[token] = entry.token
                it[expiresAt] = entry.expiresAt
                it[usedAt] = entry.usedAt
                it[createdAt] = entry.createdAt
            }
            entry
        }
    }

    override suspend fun findValidToken(token: String): PasswordReset? {
        return suspendTransaction(db) {
            PasswordResets.selectAll()
                .where {
                    (PasswordResets.token eq token) and
                    (PasswordResets.usedAt.isNull()) and
                    (PasswordResets.expiresAt greater LocalDateTime.now())
                }
                .map { it.toPasswordReset() }
                .singleOrNull()
        }
    }

    override suspend fun markUsed(id: UUID): Boolean {
        return suspendTransaction(db) {
            PasswordResets.update({ PasswordResets.id eq id }) {
                it[usedAt] = LocalDateTime.now()
            } > 0
        }
    }
}
