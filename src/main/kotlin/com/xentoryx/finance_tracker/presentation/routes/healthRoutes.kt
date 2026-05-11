// presentation/routes/healthRoutes.kt
package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.data.table.Users
import com.xentoryx.finance_tracker.domain.usecase.auth.RegisterUseCase
import com.xentoryx.finance_tracker.plugins.isDatabaseReady
import com.xentoryx.finance_tracker.presentation.dto.response.HealthErrorResponse
import com.xentoryx.finance_tracker.presentation.dto.response.HealthResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Route.healthRoutes() {
    val db by inject<R2dbcDatabase>()

    get("/health") {
        if (!isDatabaseReady) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                HealthErrorResponse(
                    status = "error",
                    message = "Database not ready yet, please wait..."
                )
            )
            return@get
        }

        try {
            val count = suspendTransaction(db) {
                Users.selectAll().count()
            }

            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "ok",
                    database = "connected",
                    userCount = count
                )
            )

        } catch (e: Exception) {
            application.log.error("Health check failed", e)

            call.respond(
                HttpStatusCode.ServiceUnavailable,
                HealthErrorResponse(
                    status = "error",
                    message = e.message ?: "unknown error"
                )
            )
        }
    }
}