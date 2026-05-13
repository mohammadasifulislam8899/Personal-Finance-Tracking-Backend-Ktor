package com.xentoryx.finance_tracker.domain.model

import java.math.BigDecimal
import java.util.UUID

data class DashboardSummary(
    val totalBalance: BigDecimal,           // সব active account এর balance যোগ
    val totalIncome: BigDecimal,            // এই মাসের total income
    val totalExpense: BigDecimal,           // এই মাসের total expense
    val savingsRate: Double,                // (income - expense) / income * 100
    val accountSummaries: List<AccountSummary>,
    val categoryBreakdown: List<CategoryBreakdown>,
    val recentTransactions: List<Transaction>
)

data class AccountSummary(
    val accountId: UUID,
    val accountName: String,
    val accountType: String,
    val balance: BigDecimal,
    val currencyCode: String
)

data class CategoryBreakdown(
    val categoryId: UUID,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String?,
    val type: String,                       // "INCOME" | "EXPENSE"
    val total: BigDecimal,
    val percentage: Double                  // total / overall * 100
)

data class MonthlyTrend(
    val month: String,                      // "2025-05"
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val net: BigDecimal                     // income - expense
)