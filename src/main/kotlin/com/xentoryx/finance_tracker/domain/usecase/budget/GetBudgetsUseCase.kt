package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.model.BudgetWithProgress
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.domain.repository.dashboard.DashboardRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

class GetBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(userId: UUID): List<BudgetWithProgress> {
        val budgets = budgetRepository.findAllByUserId(userId)
        if (budgets.isEmpty()) return emptyList()

        // FIX: N+1 query fixed — fetch all category breakdowns in ONE call
        // Use the widest date range covering all budgets
        val minDate = budgets.minOf { it.startDate }
        val maxDate = budgets.maxOf { it.endDate }

        val allBreakdowns = dashboardRepository.getCategoryBreakdown(
            userId = userId,
            type   = "EXPENSE",
            from   = minDate,
            to     = maxDate
        ).associateBy { it.categoryId }

        val allCategories = categoryRepository.findAllByUserId(userId)
            .associateBy { it.id }

        return budgets.map { budget ->
            val category  = allCategories[budget.categoryId]
            val spent     = allBreakdowns[budget.categoryId]?.total ?: BigDecimal.ZERO
            val remaining = budget.amountLimit - spent
            val percentage = if (budget.amountLimit > BigDecimal.ZERO)
                spent.divide(budget.amountLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            else 0.0

            BudgetWithProgress(
                budget        = budget,
                categoryName  = category?.name ?: "Unknown",
                categoryIcon  = category?.icon,
                categoryColor = category?.color,
                spent         = spent,
                remaining     = remaining,
                percentage    = percentage,
                isExceeded    = spent > budget.amountLimit
            )
        }
    }

    suspend operator fun invoke(id: UUID, userId: UUID): BudgetWithProgress {
        val budget = budgetRepository.findById(id)
            ?: throw NotFoundException("Budget not found")

        if (budget.userId != userId)
            throw NotFoundException("Budget not found")

        val category = categoryRepository.findById(budget.categoryId)
        val spent    = dashboardRepository.getCategoryBreakdown(
            userId = userId, type = "EXPENSE",
            from   = budget.startDate, to = budget.endDate
        ).find { it.categoryId == budget.categoryId }?.total ?: BigDecimal.ZERO

        val remaining  = budget.amountLimit - spent
        val percentage = if (budget.amountLimit > BigDecimal.ZERO)
            spent.divide(budget.amountLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        else 0.0

        return BudgetWithProgress(
            budget        = budget,
            categoryName  = category?.name ?: "Unknown",
            categoryIcon  = category?.icon,
            categoryColor = category?.color,
            spent         = spent,
            remaining     = remaining,
            percentage    = percentage,
            isExceeded    = spent > budget.amountLimit
        )
    }
}
