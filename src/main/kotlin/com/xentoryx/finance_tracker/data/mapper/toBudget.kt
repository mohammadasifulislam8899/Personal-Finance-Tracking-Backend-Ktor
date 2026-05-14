package com.xentoryx.finance_tracker.data.mapper

import com.xentoryx.finance_tracker.data.table.Budgets
import com.xentoryx.finance_tracker.domain.model.Budget
import com.xentoryx.finance_tracker.domain.model.BudgetPeriod
import org.jetbrains.exposed.v1.core.ResultRow

// ==========================
// Budget Mapper
// ==========================

fun ResultRow.toBudget(): Budget {
    return Budget(
        id          = this[Budgets.id].value,
        userId      = this[Budgets.userId].value,
        categoryId  = this[Budgets.categoryId].value,
        amountLimit = this[Budgets.amountLimit],
        period      = BudgetPeriod.valueOf(this[Budgets.period]),
        startDate   = this[Budgets.startDate],
        endDate     = this[Budgets.endDate]
    )
}

fun Iterable<ResultRow>.toBudgets(): List<Budget> = map { it.toBudget() }