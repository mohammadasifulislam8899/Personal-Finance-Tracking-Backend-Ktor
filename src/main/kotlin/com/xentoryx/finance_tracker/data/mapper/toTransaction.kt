package com.xentoryx.finance_tracker.data.mapper

import com.xentoryx.finance_tracker.data.table.Transactions
import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import org.jetbrains.exposed.v1.core.ResultRow


fun ResultRow.toTransaction(): Transaction {
    return Transaction(
        id = this[Transactions.id].value,
        userId = this[Transactions.userId].value,
        accountId = this[Transactions.accountId].value,
        categoryId = this[Transactions.categoryId].value,
        transferToAccountId = this[Transactions.transferToAccount]?.value,
        amount = this[Transactions.amount],
        type = TransactionType.valueOf(this[Transactions.type]),
        note = this[Transactions.note],
        transactionDate = this[Transactions.transactionDate],
        createdAt = this[Transactions.createdAt]
    )
}

fun Iterable<ResultRow>.toTransactions(): List<Transaction> = map { it.toTransaction() }