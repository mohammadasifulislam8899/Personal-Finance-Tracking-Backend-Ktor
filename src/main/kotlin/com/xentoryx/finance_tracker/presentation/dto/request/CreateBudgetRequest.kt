package com.xentoryx.finance_tracker.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateBudgetRequest(
    val categoryId: String,
    val amountLimit: Double,
    val period: String,             // "WEEKLY" | "MONTHLY" | "YEARLY"
    val startDate: String? = null,  // null হলে এই মাসের ১ তারিখ
    val endDate: String? = null     // null হলে period অনুযায়ী auto-calculate
)

@Serializable
data class UpdateBudgetRequest(
    val amountLimit: Double,
    val period: String,
    val startDate: String? = null,
    val endDate: String? = null
)