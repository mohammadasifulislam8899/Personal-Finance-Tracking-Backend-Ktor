package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.AccountSummary
import com.xentoryx.finance_tracker.domain.model.CategoryBreakdown
import com.xentoryx.finance_tracker.domain.model.DashboardSummary
import com.xentoryx.finance_tracker.domain.model.MonthlyTrend
import com.xentoryx.finance_tracker.presentation.dto.response.*

fun DashboardSummary.toDashboardResponse() = DashboardSummaryResponse(
    totalBalance      = totalBalance.toDouble(),
    totalIncome       = totalIncome.toDouble(),
    totalExpense      = totalExpense.toDouble(),
    savingsRate       = savingsRate,
    accounts          = accountSummaries.map { it.toAccountSummaryResponse() },
    expenseBreakdown  = categoryBreakdown.map { it.toCategoryBreakdownResponse() },
    recentTransactions = recentTransactions.map { it.toTransactionResponse() }
)

fun AccountSummary.toAccountSummaryResponse() = AccountSummaryResponse(
    accountId    = accountId.toString(),
    accountName  = accountName,
    accountType  = accountType,
    balance      = balance.toDouble(),
    currencyCode = currencyCode
)

fun CategoryBreakdown.toCategoryBreakdownResponse() = CategoryBreakdownResponse(
    categoryId    = categoryId.toString(),
    categoryName  = categoryName,
    categoryIcon  = categoryIcon,
    categoryColor = categoryColor,
    type          = type,
    total         = total.toDouble(),
    percentage    = percentage
)

fun MonthlyTrend.toMonthlyTrendResponse() = MonthlyTrendResponse(
    month        = month,
    totalIncome  = totalIncome.toDouble(),
    totalExpense = totalExpense.toDouble(),
    net          = net.toDouble()
)

fun List<MonthlyTrend>.toMonthlyTrendResponseList() = map { it.toMonthlyTrendResponse() }