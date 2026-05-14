package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import java.util.UUID

class DeleteRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = recurringRepository.delete(id, userId)
        if (!deleted) throw IllegalArgumentException("Recurring transaction not found")
    }
}