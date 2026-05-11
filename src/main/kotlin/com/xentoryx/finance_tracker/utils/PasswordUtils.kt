package com.xentoryx.finance_tracker.utils

import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {
    fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
    fun checkPassword(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}
