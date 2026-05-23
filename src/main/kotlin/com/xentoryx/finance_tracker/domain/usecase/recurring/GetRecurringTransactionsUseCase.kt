package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetRecurringTransactionsUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(userId: UUID): List<RecurringTransaction> {
        return recurringRepository.findAllByUserId(userId)
    }

    suspend operator fun invoke(id: UUID, userId: UUID): RecurringTransaction {
        val recurring = recurringRepository.findById(id)
            ?: throw NotFoundException("Recurring transaction not found")

        if (recurring.userId != userId)
            throw NotFoundException("Recurring transaction not found")

        return recurring
    }
}
