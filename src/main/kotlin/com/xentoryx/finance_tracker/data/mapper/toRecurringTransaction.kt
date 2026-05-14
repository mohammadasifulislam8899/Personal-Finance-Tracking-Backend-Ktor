package com.xentoryx.finance_tracker.data.mapper

import com.xentoryx.finance_tracker.data.table.RecurringTransactions
import com.xentoryx.finance_tracker.domain.model.RecurringFrequency
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import org.jetbrains.exposed.v1.core.ResultRow

// ==========================
// RecurringTransaction Mapper
// ==========================

fun ResultRow.toRecurringTransaction(): RecurringTransaction {
    return RecurringTransaction(
        id          = this[RecurringTransactions.id].value,
        userId      = this[RecurringTransactions.userId].value,
        accountId   = this[RecurringTransactions.accountId].value,
        categoryId  = this[RecurringTransactions.categoryId].value,
        amount      = this[RecurringTransactions.amount],
        type        = TransactionType.valueOf(this[RecurringTransactions.type]),
        frequency   = RecurringFrequency.valueOf(this[RecurringTransactions.frequency]),
        note        = this[RecurringTransactions.note],
        startDate   = this[RecurringTransactions.startDate],
        endDate     = this[RecurringTransactions.endDate],
        nextRunDate = this[RecurringTransactions.nextRunDate],
        isActive    = this[RecurringTransactions.isActive],
        createdAt   = this[RecurringTransactions.createdAt]
    )
}

fun Iterable<ResultRow>.toRecurringTransactions(): List<RecurringTransaction> =
    map { it.toRecurringTransaction() }