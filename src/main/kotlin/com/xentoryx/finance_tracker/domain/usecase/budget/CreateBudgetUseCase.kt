package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.model.Budget
import com.xentoryx.finance_tracker.domain.model.BudgetPeriod
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.presentation.dto.request.CreateBudgetRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class CreateBudgetUseCase(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateBudgetRequest): Budget {

        val limit = BigDecimal.valueOf(req.amountLimit)
        if (limit <= BigDecimal.ZERO)
            throw IllegalArgumentException("Budget limit must be greater than zero")

        val period = runCatching { BudgetPeriod.valueOf(req.period.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid period. Must be WEEKLY, MONTHLY or YEARLY") }

        val categoryId = runCatching { UUID.fromString(req.categoryId) }
            .getOrElse { throw IllegalArgumentException("Invalid category id") }

        // Category exist করে কিনা check
        categoryRepository.findById(categoryId)
            ?: throw IllegalArgumentException("Category not found")

        // Same category তে budget already আছে কিনা check
        budgetRepository.findByUserIdAndCategory(userId, categoryId)?.let {
            throw IllegalArgumentException("Budget already exists for this category")
        }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw IllegalArgumentException("Invalid startDate format. Use yyyy-MM-dd") }
        } ?: LocalDate.now().withDayOfMonth(1)

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw IllegalArgumentException("Invalid endDate format. Use yyyy-MM-dd") }
        } ?: when (period) {
            BudgetPeriod.WEEKLY  -> startDate.plusWeeks(1).minusDays(1)
            BudgetPeriod.MONTHLY -> startDate.withDayOfMonth(startDate.lengthOfMonth())
            BudgetPeriod.YEARLY  -> startDate.withDayOfYear(startDate.lengthOfYear())
        }

        if (startDate.isAfter(endDate))
            throw IllegalArgumentException("startDate must not be after endDate")

        return budgetRepository.create(
            Budget(
                id          = UUID.randomUUID(),
                userId      = userId,
                categoryId  = categoryId,
                amountLimit = limit,
                period      = period,
                startDate   = startDate,
                endDate     = endDate
            )
        )
    }
}