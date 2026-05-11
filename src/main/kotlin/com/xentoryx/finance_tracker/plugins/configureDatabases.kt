package com.xentoryx.finance_tracker.plugins

import com.xentoryx.finance_tracker.data.table.*
import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject

var isDatabaseReady = false

fun Application.configureDatabases() {
    val db by inject<R2dbcDatabase>()
    runBlocking {
        suspendTransaction(db) {
            SchemaUtils.create(
                Users, RefreshTokens, EmailVerifications, PasswordResets,
                Accounts, Categories, Transactions, Budgets, RecurringTransactions, Attachments
            )
        }
        isDatabaseReady = true
    }
}
