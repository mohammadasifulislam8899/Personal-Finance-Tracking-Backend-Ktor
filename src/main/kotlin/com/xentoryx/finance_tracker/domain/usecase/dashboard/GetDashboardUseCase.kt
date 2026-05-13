package com.xentoryx.finance_tracker.domain.usecase.dashboard

import com.xentoryx.finance_tracker.domain.model.AccountSummary
import com.xentoryx.finance_tracker.domain.model.DashboardSummary
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.domain.repository.dashboard.DashboardRepository
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import java.time.LocalDate
import java.util.UUID

class GetDashboardUseCase(
    private val dashboardRepository: DashboardRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(userId: UUID): DashboardSummary {

        val today = LocalDate.now()
        val from  = today.withDayOfMonth(1)
        val to    = today.withDayOfMonth(today.lengthOfMonth())

        // Parallel এ সব data নিয়ে আসো
        val totalBalance    = dashboardRepository.getTotalBalance(userId)
        val totalIncome     = dashboardRepository.getTotalByType(userId, "INCOME", from, to)
        val totalExpense    = dashboardRepository.getTotalByType(userId, "EXPENSE", from, to)
        val expenseBreakdown = dashboardRepository.getCategoryBreakdown(userId, "EXPENSE", from, to)
        val accounts        = accountRepository.findAllByUserId(userId)
        val recentTx        = transactionRepository.findByUserId(userId, limit = 5, offset = 0)

        val savingsRate = if (totalIncome > java.math.BigDecimal.ZERO)
            (totalIncome - totalExpense)
                .divide(totalIncome, 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toDouble()
        else 0.0

        val accountSummaries = accounts.map { acc ->
            AccountSummary(
                accountId    = acc.id,
                accountName  = acc.name,
                accountType  = acc.type.name,
                balance      = acc.balance,
                currencyCode = acc.currencyCode
            )
        }

        return DashboardSummary(
            totalBalance         = totalBalance,
            totalIncome          = totalIncome,
            totalExpense         = totalExpense,
            savingsRate          = savingsRate,
            accountSummaries     = accountSummaries,
            categoryBreakdown    = expenseBreakdown,
            recentTransactions   = recentTx
        )
    }
}

class GetMonthlyTrendUseCase(
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(userId: UUID, months: Int = 6): List<com.xentoryx.finance_tracker.domain.model.MonthlyTrend> {
        val safeMonths = if (months < 1 || months > 12) 6 else months
        return dashboardRepository.getMonthlyTrend(userId, safeMonths)
    }
}