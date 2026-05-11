package com.xentoryx.finance_tracker.data.repository

import com.xentoryx.finance_tracker.data.table.EmailVerifications
import com.xentoryx.finance_tracker.domain.repository.auth.OtpRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.time.LocalDateTime
import java.util.UUID

class OtpRepositoryImpl(
    private val db: R2dbcDatabase
) : OtpRepository {

    override suspend fun saveOtp(userId: UUID, otp: String, expiresAt: LocalDateTime) {
        suspendTransaction(db) {
            EmailVerifications.insert {
                it[id] = UUID.randomUUID()
                it[EmailVerifications.userId] = userId
                it[EmailVerifications.otp] = otp
                it[EmailVerifications.expiresAt] = expiresAt
                it[createdAt] = LocalDateTime.now()
            }
        }
    }

    override suspend fun invalidateOldOtps(userId: UUID) {
        suspendTransaction(db) {
            EmailVerifications.deleteWhere {
                (EmailVerifications.userId eq userId) and
                (EmailVerifications.usedAt.isNull())
            }
        }
    }

    override suspend fun findValidOtp(userId: UUID, otp: String): Boolean {
        return suspendTransaction(db) {
            EmailVerifications.selectAll()
                .where {
                    (EmailVerifications.userId eq userId) and
                    (EmailVerifications.otp eq otp) and
                    (EmailVerifications.usedAt.isNull()) and
                    (EmailVerifications.expiresAt greater LocalDateTime.now())
                }
                .count() > 0
        }
    }

    override suspend fun markAsUsed(userId: UUID, otp: String) {
        suspendTransaction(db) {
            EmailVerifications.update({
                (EmailVerifications.userId eq userId) and
                (EmailVerifications.otp eq otp)
            }) {
                it[usedAt] = LocalDateTime.now()
            }
        }
    }
}