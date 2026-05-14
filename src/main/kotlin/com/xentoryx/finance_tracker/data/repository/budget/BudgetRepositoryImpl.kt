package com.xentoryx.finance_tracker.data.repository.budget

import com.xentoryx.finance_tracker.data.mapper.toBudget
import com.xentoryx.finance_tracker.data.table.Budgets
import com.xentoryx.finance_tracker.domain.model.Budget
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID

class BudgetRepositoryImpl(
    private val db: R2dbcDatabase
) : BudgetRepository {

    override suspend fun create(budget: Budget): Budget {
        return suspendTransaction(db) {
            Budgets.insert {
                it[id]          = budget.id
                it[userId]      = budget.userId
                it[categoryId]  = budget.categoryId
                it[amountLimit] = budget.amountLimit
                it[period]      = budget.period.name
                it[startDate]   = budget.startDate
                it[endDate]     = budget.endDate
            }
            budget
        }
    }

    override suspend fun findById(id: UUID): Budget? {
        return suspendTransaction(db) {
            Budgets.selectAll()
                .where { Budgets.id eq id }
                .map { it.toBudget() }
                .singleOrNull()
        }
    }

    override suspend fun findAllByUserId(userId: UUID): List<Budget> {
        return suspendTransaction(db) {
            Budgets.selectAll()
                .where { Budgets.userId eq userId }
                .orderBy(Budgets.startDate)
                .map { it.toBudget() }
                .toList()
        }
    }

    override suspend fun findByUserIdAndCategory(
        userId: UUID,
        categoryId: UUID
    ): Budget? {
        return suspendTransaction(db) {
            Budgets.selectAll()
                .where {
                    (Budgets.userId eq userId) and
                    (Budgets.categoryId eq categoryId)
                }
                .map { it.toBudget() }
                .singleOrNull()
        }
    }

    override suspend fun update(budget: Budget): Budget {
        return suspendTransaction(db) {
            Budgets.update({
                (Budgets.id eq budget.id) and
                (Budgets.userId eq budget.userId)
            }) {
                it[amountLimit] = budget.amountLimit
                it[period]      = budget.period.name
                it[startDate]   = budget.startDate
                it[endDate]     = budget.endDate
            }
            budget
        }
    }

    override suspend fun delete(id: UUID, userId: UUID): Boolean {
        return suspendTransaction(db) {
            Budgets.deleteWhere {
                (Budgets.id eq id) and
                (Budgets.userId eq userId)
            } > 0
        }
    }
}