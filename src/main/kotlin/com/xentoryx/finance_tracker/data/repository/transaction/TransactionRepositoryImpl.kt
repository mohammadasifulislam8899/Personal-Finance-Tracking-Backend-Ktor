package com.xentoryx.finance_tracker.data.repository.transaction

import com.xentoryx.finance_tracker.data.mapper.toTransaction
import com.xentoryx.finance_tracker.data.table.Accounts
import com.xentoryx.finance_tracker.data.table.Categories
import com.xentoryx.finance_tracker.data.table.Transactions
import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.minus
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.time.LocalDate
import java.util.UUID

class TransactionRepositoryImpl(
    private val db: R2dbcDatabase
) : TransactionRepository {

    override suspend fun create(transaction: Transaction): Transaction {
        return suspendTransaction(db) {

            // ── 0. Category existence validate করো ────────────────────────
            val categoryExists = Categories
                .selectAll()
                .where { Categories.id eq transaction.categoryId }
                .count() > 0

            if (!categoryExists) {
                throw IllegalArgumentException("Category not found: ${transaction.categoryId}")
            }

            // ── 1. Transaction row insert ──────────────────────────────────
            Transactions.insert {
                it[id]                = transaction.id
                it[userId]            = transaction.userId
                it[accountId]         = transaction.accountId
                it[categoryId]        = transaction.categoryId
                it[transferToAccount] = transaction.transferToAccountId
                it[amount]            = transaction.amount
                it[type]              = transaction.type.name
                it[note]              = transaction.note
                it[transactionDate]   = transaction.transactionDate
            }

            // ── 2. Account balance update (same transaction = atomic) ──────
            when (transaction.type) {

                TransactionType.INCOME -> {
                    Accounts.update({ Accounts.id eq transaction.accountId }) {
                        it[balance] = balance + transaction.amount
                    }
                }

                TransactionType.EXPENSE -> {
                    Accounts.update({ Accounts.id eq transaction.accountId }) {
                        it[balance] = balance - transaction.amount
                    }
                }

                TransactionType.TRANSFER -> {
                    // Source account থেকে কাটো
                    Accounts.update({ Accounts.id eq transaction.accountId }) {
                        it[balance] = balance - transaction.amount
                    }
                    // Destination account এ যোগ করো
                    transaction.transferToAccountId?.let { toId ->
                        Accounts.update({ Accounts.id eq toId }) {
                            it[balance] = balance + transaction.amount
                        }
                    }
                }
            }

            transaction
        }
    }

    override suspend fun findById(id: UUID): Transaction? {
        return suspendTransaction(db) {
            Transactions.selectAll()
                .where { Transactions.id eq id }
                .map { it.toTransaction() }
                .singleOrNull()
        }
    }

    override suspend fun findByUserId(
        userId: UUID,
        limit: Int,
        offset: Long
    ): List<Transaction> {
        return suspendTransaction(db) {
            Transactions.selectAll()
                .where { Transactions.userId eq userId }
                .orderBy(Transactions.transactionDate, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { it.toTransaction() }
                .toList()
        }
    }

    override suspend fun findByUserIdAndDateRange(
        userId: UUID,
        from: LocalDate,
        to: LocalDate
    ): List<Transaction> {
        return suspendTransaction(db) {
            Transactions.selectAll()
                .where {
                    (Transactions.userId eq userId) and
                            (Transactions.transactionDate greaterEq from) and
                            (Transactions.transactionDate lessEq to)
                }
                .orderBy(Transactions.transactionDate, SortOrder.DESC)
                .map { it.toTransaction() }
                .toList()
        }
    }

    override suspend fun update(transaction: Transaction): Transaction {
        return suspendTransaction(db) {

            // ── পুরনো transaction load করো balance rollback এর জন্য ──────
            val old = Transactions.selectAll()
                .where { Transactions.id eq transaction.id }
                .map { it.toTransaction() }
                .singleOrNull()
                ?: throw IllegalArgumentException("Transaction not found")

            // ── পুরনো balance effect উল্টে দাও ──────────────────────────
            when (old.type) {
                TransactionType.INCOME -> {
                    Accounts.update({ Accounts.id eq old.accountId }) {
                        it[balance] = balance - old.amount
                    }
                }
                TransactionType.EXPENSE -> {
                    Accounts.update({ Accounts.id eq old.accountId }) {
                        it[balance] = balance + old.amount
                    }
                }
                TransactionType.TRANSFER -> {
                    Accounts.update({ Accounts.id eq old.accountId }) {
                        it[balance] = balance + old.amount
                    }
                    old.transferToAccountId?.let { toId ->
                        Accounts.update({ Accounts.id eq toId }) {
                            it[balance] = balance - old.amount
                        }
                    }
                }
            }

            // ── নতুন transaction row update ───────────────────────────────
            Transactions.update({ Transactions.id eq transaction.id }) {
                it[accountId]         = transaction.accountId
                it[categoryId]        = transaction.categoryId
                it[transferToAccount] = transaction.transferToAccountId
                it[amount]            = transaction.amount
                it[type]              = transaction.type.name
                it[note]              = transaction.note
                it[transactionDate]   = transaction.transactionDate
            }

            // ── নতুন balance effect apply করো ────────────────────────────
            when (transaction.type) {
                TransactionType.INCOME -> {
                    Accounts.update({ Accounts.id eq transaction.accountId }) {
                        it[balance] = balance + transaction.amount
                    }
                }
                TransactionType.EXPENSE -> {
                    Accounts.update({ Accounts.id eq transaction.accountId }) {
                        it[balance] = balance - transaction.amount
                    }
                }
                TransactionType.TRANSFER -> {
                    Accounts.update({ Accounts.id eq transaction.accountId }) {
                        it[balance] = balance - transaction.amount
                    }
                    transaction.transferToAccountId?.let { toId ->
                        Accounts.update({ Accounts.id eq toId }) {
                            it[balance] = balance + transaction.amount
                        }
                    }
                }
            }

            transaction
        }
    }

    override suspend fun delete(id: UUID, userId: UUID): Boolean {
        return suspendTransaction(db) {

            // ── Delete আগে balance rollback ───────────────────────────────
            val tx = Transactions.selectAll()
                .where {
                    (Transactions.id eq id) and
                            (Transactions.userId eq userId) // authorization check
                }
                .map { it.toTransaction() }
                .singleOrNull()
                ?: return@suspendTransaction false

            when (tx.type) {
                TransactionType.INCOME -> {
                    Accounts.update({ Accounts.id eq tx.accountId }) {
                        it[balance] = balance - tx.amount
                    }
                }
                TransactionType.EXPENSE -> {
                    Accounts.update({ Accounts.id eq tx.accountId }) {
                        it[balance] = balance + tx.amount
                    }
                }
                TransactionType.TRANSFER -> {
                    Accounts.update({ Accounts.id eq tx.accountId }) {
                        it[balance] = balance + tx.amount
                    }
                    tx.transferToAccountId?.let { toId ->
                        Accounts.update({ Accounts.id eq toId }) {
                            it[balance] = balance - tx.amount
                        }
                    }
                }
            }

            Transactions.deleteWhere {
                (Transactions.id eq id) and
                        (Transactions.userId eq userId)
            } > 0
        }
    }
}