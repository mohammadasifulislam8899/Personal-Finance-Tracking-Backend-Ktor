package com.xentoryx.finance_tracker.utils

import java.security.MessageDigest

fun String.hashSHA256(): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
