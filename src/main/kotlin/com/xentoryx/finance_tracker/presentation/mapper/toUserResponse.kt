package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.presentation.dto.response.AuthResponse
import com.xentoryx.finance_tracker.presentation.dto.response.UserResponse

fun User.toUserResponse() = UserResponse(
    id = id.toString(),
    email = email,
    fullName = fullName,
    currencyCode = currencyCode,
    isEmailVerified = isEmailVerified,
    isActive = isActive
)

fun toAuthResponse(user: User, accessToken: String, refreshToken: String) = AuthResponse(
    user = user.toUserResponse(),
    accessToken = accessToken,
    refreshToken = refreshToken
)