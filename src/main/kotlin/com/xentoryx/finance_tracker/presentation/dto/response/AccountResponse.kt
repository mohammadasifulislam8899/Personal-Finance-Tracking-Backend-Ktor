package com.xentoryx.finance_tracker.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    val id: String,
    val name: String,
    val type: String,
    val balance: Double,
    val currencyCode: String,
    val isActive: Boolean,
    val createdAt: String
)