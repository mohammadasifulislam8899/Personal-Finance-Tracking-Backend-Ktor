package com.xentoryx.finance_tracker.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

enum class AccountType { BANK, CASH, MOBILE, CREDIT_CARD, OTHER }

data class Account(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val type: AccountType,
    val balance: BigDecimal,
    val currencyCode: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)