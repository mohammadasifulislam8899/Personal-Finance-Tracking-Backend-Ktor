package com.xentoryx.finance_tracker.data.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime

object Accounts : UUIDTable("accounts") {
    val userId = reference("user_id", Users.id)
    val name = varchar("name", 100)
    val type = varchar("type", 50) // e.g., Bank, Cash, Mobile
    val balance = decimal("balance", 20, 2).default(java.math.BigDecimal.ZERO)
    val currencyCode = varchar("currency_code", 10)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
}

object Categories : UUIDTable("categories") {
    val userId = reference("user_id", Users.id).nullable() // System categories will have null
    val parentId = reference("parent_id", Categories.id).nullable()
    val name = varchar("name", 100)
    val type = varchar("type", 50) // Income or Expense
    val icon = varchar("icon", 100).nullable()
    val color = varchar("color", 20).nullable()
    val isSystem = bool("is_system").default(false)
}

object Transactions : UUIDTable("transactions") {
    val userId = reference("user_id", Users.id)
    val accountId = reference("account_id", Accounts.id)
    val categoryId = reference("category_id", Categories.id)
    val transferToAccount = reference("transfer_to_account", Accounts.id).nullable()
    val amount = decimal("amount", 20, 2)
    val type = varchar("type", 50) // Income, Expense, Transfer
    val note = text("note").nullable()
    val transactionDate = date("transaction_date")
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
}