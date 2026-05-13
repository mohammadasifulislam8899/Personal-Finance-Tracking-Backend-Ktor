package com.xentoryx.finance_tracker.domain.repository.category

import com.xentoryx.finance_tracker.domain.model.Category
import java.util.UUID

interface CategoryRepository {

    suspend fun create(category: Category): Category

    suspend fun findById(id: UUID): Category?

    // User এর নিজের + system categories একসাথে
    suspend fun findAllByUserId(userId: UUID): List<Category>

    suspend fun findSystemCategories(): List<Category>

    suspend fun update(category: Category): Category

    suspend fun delete(id: UUID, userId: UUID): Boolean

    suspend fun existsByName(userId: UUID, name: String, type: String): Boolean
}