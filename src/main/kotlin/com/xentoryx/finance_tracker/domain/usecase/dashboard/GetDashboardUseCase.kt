package com.xentoryx.finance_tracker.domain.usecase.dashboard

import com.xentoryx.finance_tracker.domain.model.AccountSummary
import com.xentoryx.finance_tracker.domain.model.DashboardSummary
import com.xentoryx.finance_tracker.domain.model.MonthlyTrend
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.domain.repository.dashboard.DashboardRepository
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

class GetDashboardUseCase(
    private val dashboardRepository: DashboardRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(userId: UUID): DashboardSummary = coroutineScope {
        val today = LocalDate.now()
        val from  = today.withDayOfMonth(1)
        val to    = today.withDayOfMonth(today.lengthOfMonth())

        // FIX: truly parallel DB calls using async/await
        val balanceDeferred   = async { dashboardRepository.getTotalBalance(userId) }
        val incomeDeferred    = async { dashboardRepository.getTotalByType(userId, "INCOME", from, to) }
        val expenseDeferred   = async { dashboardRepository.getTotalByType(userId, "EXPENSE", from, to) }
        val breakdownDeferred = async { dashboardRepository.getCategoryBreakdown(userId, "EXPENSE", from, to) }
        val accountsDeferred  = async { accountRepository.findAllByUserId(userId) }
        val recentTxDeferred  = async { transactionRepository.findByUserId(userId, limit = 5, offset = 0) }

        val totalBalance     = balanceDeferred.await()
        val totalIncome      = incomeDeferred.await()
        val totalExpense     = expenseDeferred.await()
        val expenseBreakdown = breakdownDeferred.await()
        val accounts         = accountsDeferred.await()
        val recentTx         = recentTxDeferred.await()

        val savingsRate = if (totalIncome > BigDecimal.ZERO)
            (totalIncome - totalExpense)
                .divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        else 0.0

        DashboardSummary(
            totalBalance       = totalBalance,
            totalIncome        = totalIncome,
            totalExpense       = totalExpense,
            savingsRate        = savingsRate,
            accountSummaries   = accounts.map { acc ->
                AccountSummary(acc.id, acc.name, acc.type.name, acc.balance, acc.currencyCode)
            },
            categoryBreakdown  = expenseBreakdown,
            recentTransactions = recentTx
        )
    }
}

class GetMonthlyTrendUseCase(
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(userId: UUID, months: Int = 6): List<MonthlyTrend> {
        val safeMonths = if (months < 1 || months > 12) 6 else months
        return dashboardRepository.getMonthlyTrend(userId, safeMonths)
    }
}
