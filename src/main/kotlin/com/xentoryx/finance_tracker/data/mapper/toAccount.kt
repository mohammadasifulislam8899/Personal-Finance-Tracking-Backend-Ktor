package com.xentoryx.finance_tracker.data.mapper

import com.xentoryx.finance_tracker.data.table.Accounts
import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.model.AccountType
import org.jetbrains.exposed.v1.core.ResultRow

// ==========================
// Account Mapper
// ==========================

fun ResultRow.toAccount(): Account {
    return Account(
        id           = this[Accounts.id].value,
        userId       = this[Accounts.userId].value,
        name         = this[Accounts.name],
        type         = AccountType.valueOf(this[Accounts.type]),
        balance      = this[Accounts.balance],
        currencyCode = this[Accounts.currencyCode],
        isActive     = this[Accounts.isActive],
        createdAt    = this[Accounts.createdAt]
    )
}

fun Iterable<ResultRow>.toAccounts(): List<Account> = map { it.toAccount() }