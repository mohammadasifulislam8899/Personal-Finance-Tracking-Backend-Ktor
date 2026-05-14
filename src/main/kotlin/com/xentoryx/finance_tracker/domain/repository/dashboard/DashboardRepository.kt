package com.xentoryx.finance_tracker.domain.repository.dashboard

import com.xentoryx.finance_tracker.domain.model.CategoryBreakdown
import com.xentoryx.finance_tracker.domain.model.MonthlyTrend
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

interface DashboardRepository {

    suspend fun getTotalBalance(userId: UUID): BigDecimal

    suspend fun getTotalByType(
        userId: UUID,
        type: String,
        from: LocalDate,
        to: LocalDate
    ): BigDecimal

    suspend fun getCategoryBreakdown(
        userId: UUID,
        type: String,
        from: LocalDate,
        to: LocalDate
    ): List<CategoryBreakdown>

    suspend fun getMonthlyTrend(
        userId: UUID,
        months: Int                         // কত মাসের trend চাই, default 6
    ): List<MonthlyTrend>
}