package com.xentoryx.finance_tracker.data.repository.account

import com.xentoryx.finance_tracker.data.mapper.toAccount
import com.xentoryx.finance_tracker.data.table.Accounts
import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID

class AccountRepositoryImpl(
    private val db: R2dbcDatabase
) : AccountRepository {

    override suspend fun create(account: Account): Account {
        return suspendTransaction(db) {
            Accounts.insert {
                it[id]           = account.id
                it[userId]       = account.userId
                it[name]         = account.name
                it[type]         = account.type.name
                it[balance]      = account.balance
                it[currencyCode] = account.currencyCode
                it[isActive]     = account.isActive
            }
            account
        }
    }

    override suspend fun findById(id: UUID): Account? {
        return suspendTransaction(db) {
            Accounts.selectAll()
                .where {
                    (Accounts.id eq id) and
                    (Accounts.isActive eq true)
                }
                .map { it.toAccount() }
                .singleOrNull()
        }
    }

    override suspend fun findAllByUserId(userId: UUID): List<Account> {
        return suspendTransaction(db) {
            Accounts.selectAll()
                .where {
                    (Accounts.userId eq userId) and
                    (Accounts.isActive eq true)
                }
                .map { it.toAccount() }
                .toList()
        }
    }

    override suspend fun update(account: Account): Account {
        return suspendTransaction(db) {
            Accounts.update({
                (Accounts.id eq account.id) and
                (Accounts.userId eq account.userId)
            }) {
                it[name]         = account.name
                it[type]         = account.type.name
                it[currencyCode] = account.currencyCode
            }
            account
        }
    }

    override suspend fun softDelete(id: UUID, userId: UUID): Boolean {
        return suspendTransaction(db) {
            Accounts.update({
                (Accounts.id eq id) and
                (Accounts.userId eq userId)
            }) {
                it[isActive] = false
            } > 0
        }
    }
}