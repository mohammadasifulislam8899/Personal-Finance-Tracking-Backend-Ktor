package com.xentoryx.finance_tracker.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.ApplicationEnvironment
import java.util.Date

class JwtService(private val environment: ApplicationEnvironment) {
    private val secret = environment.config.property("jwt.secret").getString()
    private val issuer = environment.config.property("jwt.issuer").getString()
    private val audience = environment.config.property("jwt.audience").getString()
    private val expiresIn = environment.config.property("jwt.expiresIn").getString().toLong()
    private val refreshExpiresIn = environment.config.property("jwt.refreshExpiresIn").getString().toLong()

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(userId: String): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
        .sign(algorithm)

    fun generateRefreshToken(userId: String): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + refreshExpiresIn))
        .sign(algorithm)

    fun verifier() = JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build()
}
