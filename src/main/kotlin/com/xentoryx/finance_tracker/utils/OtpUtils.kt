package com.xentoryx.finance_tracker.utils

import java.security.SecureRandom

object OtpUtils {
    private val secureRandom = SecureRandom()
    fun generateOtp(): String = (secureRandom.nextInt(900000) + 100000).toString()
}
