package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import java.util.UUID

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val existing = categoryRepository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        if (existing.isSystem)
            throw IllegalArgumentException("System categories cannot be deleted")

        val deleted = categoryRepository.delete(id, userId)
        if (!deleted) throw IllegalArgumentException("Category not found")
    }
}