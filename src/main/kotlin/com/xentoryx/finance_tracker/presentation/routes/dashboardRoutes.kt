package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.dashboard.GetDashboardUseCase
import com.xentoryx.finance_tracker.domain.usecase.dashboard.GetMonthlyTrendUseCase
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import com.xentoryx.finance_tracker.presentation.mapper.toDashboardResponse
import com.xentoryx.finance_tracker.presentation.mapper.toMonthlyTrendResponseList
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.dashboardRoutes() {

    val getDashboardUseCase    by inject<GetDashboardUseCase>()
    val getMonthlyTrendUseCase by inject<GetMonthlyTrendUseCase>()

    authenticate("auth-jwt") {
        route("/dashboard") {

            // GET /dashboard
            get {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val summary = getDashboardUseCase(userId)
                call.respond(summary.toDashboardResponse())
            }

            // GET /dashboard/trend?months=6
            get("/trend") {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val months = call.request.queryParameters["months"]
                    ?.toIntOrNull() ?: 6

                val trend = getMonthlyTrendUseCase(userId, months)
                call.respond(trend.toMonthlyTrendResponseList())
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.userId(): UUID? {
    val raw = principal<JWTPrincipal>()
        ?.payload?.getClaim("userId")?.asString() ?: return null
    return runCatching { UUID.fromString(raw) }.getOrNull()
}