package com.xentoryx.finance_tracker.domain.repository.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import java.time.LocalDate
import java.util.UUID

interface TransactionRepository {

    suspend fun create(transaction: Transaction): Transaction

    suspend fun findById(id: UUID): Transaction?

    suspend fun findByUserId(
        userId: UUID,
        limit: Int,
        offset: Long
    ): List<Transaction>

    suspend fun findByUserIdAndDateRange(
        userId: UUID,
        from: LocalDate,
        to: LocalDate
    ): List<Transaction>

    suspend fun update(transaction: Transaction): Transaction

    suspend fun delete(id: UUID, userId: UUID): Boolean
}