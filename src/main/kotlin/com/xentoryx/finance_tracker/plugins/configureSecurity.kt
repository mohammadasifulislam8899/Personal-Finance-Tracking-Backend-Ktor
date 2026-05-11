package com.xentoryx.finance_tracker.plugins

import com.xentoryx.finance_tracker.security.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val jwtService by inject<JwtService>()
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtService.verifier())
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
