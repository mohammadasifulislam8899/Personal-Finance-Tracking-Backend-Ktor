package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import java.util.UUID

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val existing = categoryRepository.findById(id)
            ?: throw NotFoundException("Category not found")

        if (existing.isSystem)
            throw ValidationException("System categories cannot be deleted")

        val deleted = categoryRepository.delete(id, userId)
        if (!deleted) throw NotFoundException("Category not found")
    }
}
