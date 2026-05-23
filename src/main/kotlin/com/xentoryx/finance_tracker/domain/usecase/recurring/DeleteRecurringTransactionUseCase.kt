package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class DeleteRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = recurringRepository.delete(id, userId)
        if (!deleted) throw NotFoundException("Recurring transaction not found")
    }
}
