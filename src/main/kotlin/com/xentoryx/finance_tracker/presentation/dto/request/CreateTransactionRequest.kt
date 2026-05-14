package com.xentoryx.finance_tracker.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateTransactionRequest(
    val accountId: String,
    val categoryId: String,
    val transferToAccountId: String? = null,
    val amount: Double,
    val type: String,           // "INCOME" | "EXPENSE" | "TRANSFER"
    val note: String? = null,
    val transactionDate: String // "2025-05-13"
)

@Serializable
data class UpdateTransactionRequest(
    val accountId: String,
    val categoryId: String,
    val transferToAccountId: String? = null,
    val amount: Double,
    val type: String,
    val note: String? = null,
    val transactionDate: String
)

@Serializable
data class TransactionFilterRequest(
    val from: String? = null,   // "2025-05-01"
    val to: String? = null,     // "2025-05-31"
    val page: Int = 1,
    val limit: Int = 20
)