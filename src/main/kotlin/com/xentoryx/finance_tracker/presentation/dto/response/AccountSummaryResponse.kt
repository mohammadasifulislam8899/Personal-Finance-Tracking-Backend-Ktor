package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AccountSummaryResponse(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val balance: Double,
    val currencyCode: String
)

@Serializable
data class CategoryBreakdownResponse(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String?,
    val type: String,
    val total: Double,
    val percentage: Double
)

@Serializable
data class DashboardSummaryResponse(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val savingsRate: Double,
    val accounts: List<AccountSummaryResponse>,
    val expenseBreakdown: List<CategoryBreakdownResponse>,
    val recentTransactions: List<TransactionResponse>
)

@Serializable
data class MonthlyTrendResponse(
    val month: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val net: Double
)