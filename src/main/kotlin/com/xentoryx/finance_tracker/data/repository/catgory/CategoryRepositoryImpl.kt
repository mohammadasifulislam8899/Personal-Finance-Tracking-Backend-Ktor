package com.xentoryx.finance_tracker.data.repository.catgory

import com.xentoryx.finance_tracker.data.mapper.toCategory
import com.xentoryx.finance_tracker.data.table.Categories
import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID

class CategoryRepositoryImpl(
    private val db: R2dbcDatabase
) : CategoryRepository {

    override suspend fun create(category: Category): Category {
        return suspendTransaction(db) {
            Categories.insert {
                it[id]       = category.id
                it[userId]   = category.userId
                it[parentId] = category.parentId
                it[name]     = category.name
                it[type]     = category.type.name
                it[icon]     = category.icon
                it[color]    = category.color
                it[isSystem] = category.isSystem
            }
            category
        }
    }

    override suspend fun findById(id: UUID): Category? {
        return suspendTransaction(db) {
            Categories.selectAll()
                .where { Categories.id eq id }
                .map { it.toCategory() }
                .singleOrNull()
        }
    }

    override suspend fun findAllByUserId(userId: UUID): List<Category> {
        return suspendTransaction(db) {
            Categories.selectAll()
                .where {
                    // নিজের categories + system categories
                    (Categories.userId eq userId) or
                    (Categories.isSystem eq true)
                }
                .orderBy(Categories.isSystem)
                .map { it.toCategory() }
                .toList()
        }
    }

    override suspend fun findSystemCategories(): List<Category> {
        return suspendTransaction(db) {
            Categories.selectAll()
                .where { Categories.isSystem eq true }
                .map { it.toCategory() }
                .toList()
        }
    }

    override suspend fun update(category: Category): Category {
        return suspendTransaction(db) {
            Categories.update({
                (Categories.id eq category.id) and
                (Categories.userId eq category.userId)
            }) {
                it[name]  = category.name
                it[icon]  = category.icon
                it[color] = category.color
            }
            category
        }
    }

    override suspend fun delete(id: UUID, userId: UUID): Boolean {
        return suspendTransaction(db) {
            Categories.deleteWhere {
                (Categories.id eq id) and
                (Categories.userId eq userId) and
                (Categories.isSystem eq false) // system category delete করা যাবে না
            } > 0
        }
    }

    // ✅ এভাবে fix করো — system categories ও check করো
    override suspend fun existsByName(userId: UUID, name: String, type: String): Boolean {
        return suspendTransaction(db) {
            Categories.selectAll()
                .where {
                    (
                            (Categories.userId eq userId) or
                                    (Categories.isSystem eq true)
                            ) and
                            (Categories.name eq name) and
                            (Categories.type eq type)
                }
                .count() > 0
        }
    }
}