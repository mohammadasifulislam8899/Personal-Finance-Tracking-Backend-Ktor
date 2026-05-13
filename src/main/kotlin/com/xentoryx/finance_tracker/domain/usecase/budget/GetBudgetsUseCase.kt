package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.model.BudgetWithProgress
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.domain.repository.dashboard.DashboardRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class GetBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(userId: UUID): List<BudgetWithProgress> {

        val budgets = budgetRepository.findAllByUserId(userId)

        return budgets.map { budget ->

            val category = categoryRepository.findById(budget.categoryId)

            // Budget period এর মধ্যে কত খরচ হয়েছে
            val spent = dashboardRepository.getTotalByType(
                userId = userId,
                type   = "EXPENSE",
                from   = budget.startDate,
                to     = budget.endDate
            ).let { total ->
                // শুধু এই category র expense নিতে হবে
                // DashboardRepository তে category filter যোগ করা দরকার
                // এখন আপাতত full breakdown থেকে filter করছি
                dashboardRepository.getCategoryBreakdown(
                    userId = userId,
                    type   = "EXPENSE",
                    from   = budget.startDate,
                    to     = budget.endDate
                ).find { it.categoryId == budget.categoryId }?.total ?: BigDecimal.ZERO
            }

            val remaining   = budget.amountLimit - spent
            val percentage  = if (budget.amountLimit > BigDecimal.ZERO)
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
            ?: throw IllegalArgumentException("Budget not found")

        if (budget.userId != userId)
            throw IllegalArgumentException("Budget not found")

        val category = categoryRepository.findById(budget.categoryId)

        val spent = dashboardRepository.getCategoryBreakdown(
            userId = userId,
            type   = "EXPENSE",
            from   = budget.startDate,
            to     = budget.endDate
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