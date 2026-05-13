package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateCategoryRequest
import java.util.UUID

class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateCategoryRequest): Category {

        if (req.name.isBlank())
            throw IllegalArgumentException("Category name cannot be empty")

        val existing = categoryRepository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        if (existing.isSystem)
            throw IllegalArgumentException("System categories cannot be modified")

        if (existing.userId != userId)
            throw IllegalArgumentException("Category not found")

        return categoryRepository.update(
            existing.copy(
                name  = req.name.trim(),
                icon  = req.icon?.trim() ?: existing.icon,
                color = req.color?.trim() ?: existing.color
            )
        )
    }
}