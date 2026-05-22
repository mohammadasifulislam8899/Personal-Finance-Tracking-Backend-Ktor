package com.xentoryx.finance_tracker.presentation.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.userId(): UUID? {
    val raw = principal<JWTPrincipal>()
        ?.payload?.getClaim("userId")?.asString() ?: return null
    return runCatching { UUID.fromString(raw) }.getOrNull()
}
