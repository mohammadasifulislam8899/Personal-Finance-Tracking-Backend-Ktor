package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class RecurringTransactionResponse(
    val id: String,
    val accountId: String,
    val categoryId: String,
    val amount: Double,
    val type: String,
    val frequency: String,
    val note: String?,
    val startDate: String,
    val endDate: String?,
    val nextRunDate: String,
    val isActive: Boolean,
    val createdAt: String
)