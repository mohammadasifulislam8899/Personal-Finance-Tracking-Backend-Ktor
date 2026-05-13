package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.presentation.dto.response.TransactionResponse

fun Transaction.toTransactionResponse() = TransactionResponse(
    id                  = id.toString(),
    userId              = userId.toString(),
    accountId           = accountId.toString(),
    categoryId          = categoryId.toString(),
    transferToAccountId = transferToAccountId?.toString(),
    amount              = amount.toDouble(),
    type                = type.name,
    note                = note,
    transactionDate     = transactionDate.toString(),   // "2025-05-13"
    createdAt           = createdAt.toString()
)

fun List<Transaction>.toTransactionResponseList() = map { it.toTransactionResponse() }