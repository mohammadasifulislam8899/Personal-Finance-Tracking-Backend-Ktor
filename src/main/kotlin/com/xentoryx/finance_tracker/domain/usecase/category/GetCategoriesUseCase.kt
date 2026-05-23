package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID): List<Category> {
        return categoryRepository.findAllByUserId(userId)
    }

    suspend operator fun invoke(id: UUID, userId: UUID): Category {
        val category = categoryRepository.findById(id)
            ?: throw NotFoundException("Category not found")

        if (!category.isSystem && category.userId != userId)
            throw NotFoundException("Category not found")

        return category
    }
}
