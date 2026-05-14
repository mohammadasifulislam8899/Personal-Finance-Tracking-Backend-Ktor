package com.xentoryx.finance_tracker.data.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime

object Budgets : UUIDTable("budgets") {
    val userId = reference("user_id", Users.id)
    val categoryId = reference("category_id", Categories.id)
    val amountLimit = decimal("amount_limit", 20, 2)
    val period = varchar("period", 50) // Monthly, Weekly, Yearly
    val startDate = date("start_date")
    val endDate = date("end_date")
}

object RecurringTransactions : UUIDTable("recurring_transactions") {
    val userId      = reference("user_id", Users.id)
    val accountId   = reference("account_id", Accounts.id)
    val categoryId  = reference("category_id", Categories.id)
    val amount      = decimal("amount", 20, 2)
    val type        = varchar("type", 50)
    val frequency   = varchar("frequency", 50)
    val note        = varchar("note", 512).nullable()
    val startDate   = date("start_date")
    val endDate     = date("end_date").nullable()
    val nextRunDate = date("next_run_date")         // nextDueDate → nextRunDate
    val isActive    = bool("is_active").default(true)
    val createdAt   = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
}

object Attachments : UUIDTable("attachments") {
    val transactionId = reference("transaction_id", Transactions.id)
    val fileUrl = varchar("file_url", 512)
    val fileType = varchar("file_type", 50)
    val uploadedAt = datetime("uploaded_at").clientDefault { java.time.LocalDateTime.now() }
}