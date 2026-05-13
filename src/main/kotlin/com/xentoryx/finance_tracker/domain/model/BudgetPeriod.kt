package com.xentoryx.finance_tracker.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

enum class BudgetPeriod { WEEKLY, MONTHLY, YEARLY }

data class Budget(
    val id: UUID,
    val userId: UUID,
    val categoryId: UUID,
    val amountLimit: BigDecimal,
    val period: BudgetPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class BudgetWithProgress(
    val budget: Budget,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String?,
    val spent: BigDecimal,
    val remaining: BigDecimal,
    val percentage: Double,         // spent / limit * 100
    val isExceeded: Boolean
)