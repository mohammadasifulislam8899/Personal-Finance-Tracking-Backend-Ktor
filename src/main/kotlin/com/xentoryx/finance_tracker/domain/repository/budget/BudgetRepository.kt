package com.xentoryx.finance_tracker.domain.repository.budget

import com.xentoryx.finance_tracker.domain.model.Budget
import java.util.UUID

interface BudgetRepository {

    suspend fun create(budget: Budget): Budget

    suspend fun findById(id: UUID): Budget?

    suspend fun findAllByUserId(userId: UUID): List<Budget>

    suspend fun findByUserIdAndCategory(userId: UUID, categoryId: UUID): Budget?

    suspend fun update(budget: Budget): Budget

    suspend fun delete(id: UUID, userId: UUID): Boolean
}