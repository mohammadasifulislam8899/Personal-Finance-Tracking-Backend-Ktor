package com.xentoryx.finance_tracker.plugins

import com.xentoryx.finance_tracker.presentation.routes.authRoutes
import com.xentoryx.finance_tracker.presentation.routes.categoryRoutes
import com.xentoryx.finance_tracker.presentation.routes.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {

    routing {
        healthRoutes()
        authRoutes()
        categoryRoutes()
    }
}
