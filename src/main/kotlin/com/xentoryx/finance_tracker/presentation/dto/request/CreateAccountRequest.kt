package com.xentoryx.finance_tracker.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val name: String,
    val type: String,                   // "BANK" | "CASH" | "MOBILE" | "CREDIT_CARD" | "OTHER"
    val initialBalance: Double? = 0.0,
    val currencyCode: String? = "BDT"
)

@Serializable
data class UpdateAccountRequest(
    val name: String,
    val type: String,
    val currencyCode: String? = null
)