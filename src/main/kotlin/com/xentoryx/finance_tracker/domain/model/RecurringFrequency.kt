package com.xentoryx.finance_tracker.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

enum class RecurringFrequency { DAILY, WEEKLY, MONTHLY, YEARLY }

data class RecurringTransaction(
    val id: UUID,
    val userId: UUID,
    val accountId: UUID,
    val categoryId: UUID,
    val amount: BigDecimal,
    val type: TransactionType,
    val frequency: RecurringFrequency,
    val note: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val nextRunDate: LocalDate,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)