package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BudgetResponse(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String?,
    val amountLimit: Double,
    val period: String,
    val startDate: String,
    val endDate: String,
    val spent: Double,
    val remaining: Double,
    val percentage: Double,
    val isExceeded: Boolean
)