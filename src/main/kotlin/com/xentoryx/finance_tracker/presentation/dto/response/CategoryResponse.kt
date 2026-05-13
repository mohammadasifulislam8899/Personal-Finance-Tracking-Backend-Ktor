package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val type: String,
    val parentId: String?,
    val icon: String?,
    val color: String?,
    val isSystem: Boolean
)