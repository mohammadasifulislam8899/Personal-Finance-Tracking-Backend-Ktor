package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.presentation.dto.response.RecurringTransactionResponse

fun RecurringTransaction.toRecurringTransactionResponse() = RecurringTransactionResponse(
    id          = id.toString(),
    accountId   = accountId.toString(),
    categoryId  = categoryId.toString(),
    amount      = amount.toDouble(),
    type        = type.name,
    frequency   = frequency.name,
    note        = note,
    startDate   = startDate.toString(),
    endDate     = endDate?.toString(),
    nextRunDate = nextRunDate.toString(),
    isActive    = isActive,
    createdAt   = createdAt.toString()
)

fun List<RecurringTransaction>.toRecurringTransactionResponseList() =
    map { it.toRecurringTransactionResponse() }