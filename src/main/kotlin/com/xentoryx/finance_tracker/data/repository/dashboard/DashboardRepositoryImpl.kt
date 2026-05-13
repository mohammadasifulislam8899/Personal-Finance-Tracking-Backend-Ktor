package com.xentoryx.finance_tracker.data.repository.dashboard

import com.xentoryx.finance_tracker.data.table.Accounts
import com.xentoryx.finance_tracker.data.table.Categories
import com.xentoryx.finance_tracker.data.table.Transactions
import com.xentoryx.finance_tracker.domain.model.CategoryBreakdown
import com.xentoryx.finance_tracker.domain.model.MonthlyTrend
import com.xentoryx.finance_tracker.domain.repository.dashboard.DashboardRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.Sum
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class DashboardRepositoryImpl(
    private val db: R2dbcDatabase
) : DashboardRepository {

    // ── সব active account এর balance sum ─────────────────────────────────
    override suspend fun getTotalBalance(userId: UUID): BigDecimal {
        return suspendTransaction(db) {
            Accounts.selectAll()
                .where {
                    (Accounts.userId eq userId) and
                    (Accounts.isActive eq true)
                }
                .map { it[Accounts.balance] }
                .toList()
                .fold(BigDecimal.ZERO) { acc, balance -> acc + balance }
        }
    }

    // ── Date range এ total income বা expense ──────────────────────────────
    override suspend fun getTotalByType(
        userId: UUID,
        type: String,
        from: LocalDate,
        to: LocalDate
    ): BigDecimal {
        return suspendTransaction(db) {
            Transactions.selectAll()
                .where {
                    (Transactions.userId eq userId) and
                    (Transactions.type eq type) and
                    (Transactions.transactionDate greaterEq from) and
                    (Transactions.transactionDate lessEq to)
                }
                .map { it[Transactions.amount] }
                .toList()
                .fold(BigDecimal.ZERO) { acc, amount -> acc + amount }
        }
    }

    // ── Category wise breakdown ───────────────────────────────────────────
    override suspend fun getCategoryBreakdown(
        userId: UUID,
        type: String,
        from: LocalDate,
        to: LocalDate
    ): List<CategoryBreakdown> {
        return suspendTransaction(db) {

            // Transactions + Categories join
            val rows = (Transactions innerJoin Categories)
                .selectAll()
                .where {
                    (Transactions.userId eq userId) and
                    (Transactions.type eq type) and
                    (Transactions.transactionDate greaterEq from) and
                    (Transactions.transactionDate lessEq to)
                }
                .map { row ->
                    Triple(
                        first  = row[Transactions.categoryId].value,
                        second = row[Categories.name],
                        third  = Triple(
                            row[Categories.icon],
                            row[Categories.color],
                            row[Transactions.amount]
                        )
                    )
                }
                .toList()

            // Category id দিয়ে group করে sum করো
            val grouped = rows.groupBy { it.first }

            val grandTotal = rows.fold(BigDecimal.ZERO) { acc, row ->
                acc + row.third.third
            }

            grouped.map { (categoryId, entries) ->
                val total      = entries.fold(BigDecimal.ZERO) { acc, e -> acc + e.third.third }
                val name       = entries.first().second
                val icon       = entries.first().third.first
                val color      = entries.first().third.second
                val percentage = if (grandTotal > BigDecimal.ZERO)
                    total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP)
                        .toDouble()
                else 0.0

                CategoryBreakdown(
                    categoryId    = categoryId,
                    categoryName  = name,
                    categoryIcon  = icon,
                    categoryColor = color,
                    type          = type,
                    total         = total,
                    percentage    = percentage
                )
            }.sortedByDescending { it.total }
        }
    }

    // ── Last N months এর income/expense trend ────────────────────────────
    override suspend fun getMonthlyTrend(userId: UUID, months: Int): List<MonthlyTrend> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val result    = mutableListOf<MonthlyTrend>()
        val today     = LocalDate.now()

        repeat(months) { i ->
            val targetMonth = today.minusMonths(i.toLong())
            val from        = targetMonth.withDayOfMonth(1)
            val to          = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth())

            val rows = suspendTransaction(db) {
                Transactions.selectAll()
                    .where {
                        (Transactions.userId eq userId) and
                        (Transactions.transactionDate greaterEq from) and
                        (Transactions.transactionDate lessEq to)
                    }
                    .map { it[Transactions.type] to it[Transactions.amount] }
                    .toList()
            }

            val income  = rows.filter { it.first == "INCOME" }
                .fold(BigDecimal.ZERO) { acc, r -> acc + r.second }
            val expense = rows.filter { it.first == "EXPENSE" }
                .fold(BigDecimal.ZERO) { acc, r -> acc + r.second }

            result.add(
                MonthlyTrend(
                    month        = targetMonth.format(formatter),
                    totalIncome  = income,
                    totalExpense = expense,
                    net          = income - expense
                )
            )
        }

        return result.reversed() // পুরনো month আগে
    }
}