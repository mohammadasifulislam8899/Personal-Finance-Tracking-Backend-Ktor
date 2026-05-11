package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

// In your response DTOs file
@Serializable
data class HealthResponse(
    val status: String,
    val database: String,
    val userCount: Long
)

@Serializable
data class HealthErrorResponse(
    val status: String,
    val message: String
)