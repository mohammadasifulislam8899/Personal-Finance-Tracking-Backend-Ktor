package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.ValidationException
import java.time.LocalDate
import java.util.UUID

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(userId: UUID, page: Int, limit: Int): List<Transaction> {
        val safePage  = if (page < 1) 1 else page
        val safeLimit = if (limit < 1 || limit > 100) 20 else limit
        val offset    = ((safePage - 1) * safeLimit).toLong()
        return transactionRepository.findByUserId(userId, safeLimit, offset)
    }

    suspend operator fun invoke(userId: UUID, from: LocalDate, to: LocalDate): List<Transaction> {
        if (from.isAfter(to))
            throw ValidationException("'from' date must not be after 'to' date")
        return transactionRepository.findByUserIdAndDateRange(userId, from, to)
    }
}
