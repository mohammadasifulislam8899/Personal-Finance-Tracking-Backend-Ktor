package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import java.util.UUID

class DeleteBudgetUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = budgetRepository.delete(id, userId)
        if (!deleted) throw IllegalArgumentException("Budget not found")
    }
}