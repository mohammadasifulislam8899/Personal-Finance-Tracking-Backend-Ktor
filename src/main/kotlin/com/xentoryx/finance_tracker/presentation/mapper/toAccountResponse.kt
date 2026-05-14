package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.presentation.dto.response.AccountResponse

fun Account.toAccountResponse() = AccountResponse(
    id           = id.toString(),
    name         = name,
    type         = type.name,
    balance      = balance.toDouble(),
    currencyCode = currencyCode,
    isActive     = isActive,
    createdAt    = createdAt.toString()
)

fun List<Account>.toAccountResponseList() = map { it.toAccountResponse() }