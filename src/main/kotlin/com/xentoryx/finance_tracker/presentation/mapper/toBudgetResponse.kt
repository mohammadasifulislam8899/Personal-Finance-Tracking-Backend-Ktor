package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.BudgetWithProgress
import com.xentoryx.finance_tracker.presentation.dto.response.BudgetResponse

fun BudgetWithProgress.toBudgetResponse() = BudgetResponse(
    id            = budget.id.toString(),
    categoryId    = budget.categoryId.toString(),
    categoryName  = categoryName,
    categoryIcon  = categoryIcon,
    categoryColor = categoryColor,
    amountLimit   = budget.amountLimit.toDouble(),
    period        = budget.period.name,
    startDate     = budget.startDate.toString(),
    endDate       = budget.endDate.toString(),
    spent         = spent.toDouble(),
    remaining     = remaining.toDouble(),
    percentage    = percentage,
    isExceeded    = isExceeded
)

fun List<BudgetWithProgress>.toBudgetResponseList() = map { it.toBudgetResponse() }