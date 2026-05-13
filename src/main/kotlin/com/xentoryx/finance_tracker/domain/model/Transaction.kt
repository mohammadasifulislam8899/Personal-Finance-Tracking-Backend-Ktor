package com.xentoryx.finance_tracker.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Transaction(
    val id: UUID,
    val userId: UUID,
    val accountId: UUID,
    val categoryId: UUID,
    val transferToAccountId: UUID?,
    val amount: BigDecimal,
    val type: TransactionType, // enum
    val note: String?,
    val transactionDate: LocalDate,
    val createdAt: LocalDateTime
)

enum class TransactionType { INCOME, EXPENSE, TRANSFER }