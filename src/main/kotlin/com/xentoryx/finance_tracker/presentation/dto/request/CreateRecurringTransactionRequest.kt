package com.xentoryx.finance_tracker.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRecurringTransactionRequest(
    val accountId: String,
    val categoryId: String,
    val amount: Double,
    val type: String,               // "INCOME" | "EXPENSE"
    val frequency: String,          // "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY"
    val note: String? = null,
    val startDate: String? = null,  // null = আজ থেকে
    val endDate: String? = null     // null = indefinite
)

@Serializable
data class UpdateRecurringTransactionRequest(
    val accountId: String,
    val categoryId: String,
    val amount: Double,
    val type: String,
    val frequency: String,
    val note: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)