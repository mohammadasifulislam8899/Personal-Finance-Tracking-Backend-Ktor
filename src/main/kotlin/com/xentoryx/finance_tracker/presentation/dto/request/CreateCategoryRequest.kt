package com.xentoryx.finance_tracker.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val type: String,               // "INCOME" | "EXPENSE"
    val parentId: String? = null,
    val icon: String? = null,
    val color: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val icon: String? = null,
    val color: String? = null
)