package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.model.Budget
import com.xentoryx.finance_tracker.domain.model.BudgetPeriod
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateBudgetRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class UpdateBudgetUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateBudgetRequest): Budget {

        val existing = budgetRepository.findById(id)
            ?: throw NotFoundException("Budget not found")

        if (existing.userId != userId)
            throw NotFoundException("Budget not found")

        val limit = BigDecimal.valueOf(req.amountLimit)
        if (limit <= BigDecimal.ZERO)
            throw ValidationException("Budget limit must be greater than zero")

        val period = runCatching { BudgetPeriod.valueOf(req.period.uppercase()) }
            .getOrElse { throw ValidationException("Invalid period. Must be WEEKLY, MONTHLY or YEARLY") }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException("Invalid startDate format") }
        } ?: existing.startDate

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException("Invalid endDate format") }
        } ?: existing.endDate

        if (startDate.isAfter(endDate))
            throw ValidationException("startDate must not be after endDate")

        return budgetRepository.update(
            existing.copy(
                amountLimit = limit,
                period      = period,
                startDate   = startDate,
                endDate     = endDate
            )
        )
    }
}
