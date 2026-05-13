package com.xentoryx.finance_tracker.data.seeder

import com.xentoryx.finance_tracker.data.table.Categories
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.UUID

object CategorySeeder {

    private val expenseCategories = listOf(
        Triple("Food & Dining",     "🍔", "#FF6B6B"),
        Triple("Transport",         "🚗", "#4ECDC4"),
        Triple("Shopping",          "🛍️", "#45B7D1"),
        Triple("Bills & Utilities", "💡", "#96CEB4"),
        Triple("Health",            "🏥", "#FFEAA7"),
        Triple("Entertainment",     "🎬", "#DDA0DD"),
        Triple("Education",         "📚", "#98D8C8"),
        Triple("Rent",              "🏠", "#F7DC6F"),
        Triple("Groceries",         "🛒", "#82E0AA"),
        Triple("Others",            "📦", "#AEB6BF")
    )

    private val incomeCategories = listOf(
        Triple("Salary",      "💼", "#2ECC71"),
        Triple("Freelance",   "💻", "#3498DB"),
        Triple("Business",    "🏢", "#9B59B6"),
        Triple("Investment",  "📈", "#E67E22"),
        Triple("Gift",        "🎁", "#E74C3C"),
        Triple("Others",      "💰", "#1ABC9C")
    )

    suspend fun seed(db: R2dbcDatabase) {
        suspendTransaction(db) {
            // Already seeded হলে skip করো
            val count = Categories.selectAll()
                .where { Categories.isSystem eq true }
                .count()

            if (count > 0) return@suspendTransaction

            expenseCategories.forEach { (name, icon, color) ->
                Categories.insert {
                    it[id]               = UUID.randomUUID()
                    it[userId]           = null
                    it[parentId]         = null
                    it[Categories.name]  = name
                    it[type]             = "EXPENSE"
                    it[Categories.icon]  = icon
                    it[Categories.color] = color
                    it[isSystem]         = true
                }
            }

            incomeCategories.forEach { (name, icon, color) ->
                Categories.insert {
                    it[id]               = UUID.randomUUID()
                    it[userId]           = null
                    it[parentId]         = null
                    it[Categories.name]  = name
                    it[type]             = "INCOME"
                    it[Categories.icon]  = icon
                    it[Categories.color] = color
                    it[isSystem]         = true
                }
            }
        }
    }
}