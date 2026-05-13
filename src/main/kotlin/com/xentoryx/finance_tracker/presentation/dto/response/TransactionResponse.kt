package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    val id: String,
    val userId: String,
    val accountId: String,
    val categoryId: String,
    val transferToAccountId: String?,
    val amount: Double,
    val type: String,
    val note: String?,
    val transactionDate: String,
    val createdAt: String
)

@Serializable
data class TransactionListResponse(
    val data: List<TransactionResponse>,
    val page: Int,
    val limit: Int,
    val total: Int
)