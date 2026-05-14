package com.xentoryx.finance_tracker.data.repository.recurring

import com.xentoryx.finance_tracker.data.mapper.toRecurringTransaction
import com.xentoryx.finance_tracker.data.table.RecurringTransactions
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.time.LocalDate
import java.util.UUID

class RecurringTransactionRepositoryImpl(
    private val db: R2dbcDatabase
) : RecurringTransactionRepository {

    override suspend fun create(recurring: RecurringTransaction): RecurringTransaction {
        return suspendTransaction(db) {
            RecurringTransactions.insert {
                it[id]          = recurring.id
                it[userId]      = recurring.userId
                it[accountId]   = recurring.accountId
                it[categoryId]  = recurring.categoryId
                it[amount]      = recurring.amount
                it[type]        = recurring.type.name
                it[frequency]   = recurring.frequency.name
                it[note]        = recurring.note
                it[startDate]   = recurring.startDate
                it[endDate]     = recurring.endDate
                it[nextRunDate] = recurring.nextRunDate
                it[isActive]    = recurring.isActive
            }
            recurring
        }
    }

    override suspend fun findById(id: UUID): RecurringTransaction? {
        return suspendTransaction(db) {
            RecurringTransactions.selectAll()
                .where { RecurringTransactions.id eq id }
                .map { it.toRecurringTransaction() }
                .singleOrNull()
        }
    }

    override suspend fun findAllByUserId(userId: UUID): List<RecurringTransaction> {
        return suspendTransaction(db) {
            RecurringTransactions.selectAll()
                .where { RecurringTransactions.userId eq userId }
                .orderBy(RecurringTransactions.nextRunDate)
                .map { it.toRecurringTransaction() }
                .toList()
        }
    }

    override suspend fun findDueToday(today: LocalDate): List<RecurringTransaction> {
        return suspendTransaction(db) {
            RecurringTransactions.selectAll()
                .where {
                    (RecurringTransactions.isActive eq true) and
                    (RecurringTransactions.nextRunDate lessEq today)
                }
                .map { it.toRecurringTransaction() }
                .toList()
        }
    }

    override suspend fun update(recurring: RecurringTransaction): RecurringTransaction {
        return suspendTransaction(db) {
            RecurringTransactions.update({
                (RecurringTransactions.id eq recurring.id) and
                (RecurringTransactions.userId eq recurring.userId)
            }) {
                it[accountId]   = recurring.accountId
                it[categoryId]  = recurring.categoryId
                it[amount]      = recurring.amount
                it[type]        = recurring.type.name
                it[frequency]   = recurring.frequency.name
                it[note]        = recurring.note
                it[startDate]   = recurring.startDate
                it[endDate]     = recurring.endDate
                it[nextRunDate] = recurring.nextRunDate
            }
            recurring
        }
    }

    override suspend fun updateNextRunDate(id: UUID, nextRunDate: LocalDate) {
        suspendTransaction(db) {
            RecurringTransactions.update({ RecurringTransactions.id eq id }) {
                it[RecurringTransactions.nextRunDate] = nextRunDate
            }
        }
    }

    override suspend fun deactivate(id: UUID, userId: UUID): Boolean {
        return suspendTransaction(db) {
            RecurringTransactions.update({
                (RecurringTransactions.id eq id) and
                (RecurringTransactions.userId eq userId)
            }) {
                it[isActive] = false
            } > 0
        }
    }

    override suspend fun delete(id: UUID, userId: UUID): Boolean {
        return suspendTransaction(db) {
            RecurringTransactions.deleteWhere {
                (RecurringTransactions.id eq id) and
                (RecurringTransactions.userId eq userId)
            } > 0
        }
    }
}