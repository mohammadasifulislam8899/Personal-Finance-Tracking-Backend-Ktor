package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import java.util.UUID

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    // User এর সব categories (system + custom)
    suspend operator fun invoke(userId: UUID): List<Category> {
        return categoryRepository.findAllByUserId(userId)
    }

    // Single category
    suspend operator fun invoke(id: UUID, userId: UUID): Category {
        val category = categoryRepository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        // System category সবার জন্য accessible
        if (!category.isSystem && category.userId != userId)
            throw IllegalArgumentException("Category not found")

        return category
    }
}