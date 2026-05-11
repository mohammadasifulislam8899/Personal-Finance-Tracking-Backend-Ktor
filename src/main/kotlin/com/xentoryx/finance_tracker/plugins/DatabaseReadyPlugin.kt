// plugins/DatabaseReadyPlugin.kt
package com.xentoryx.finance_tracker.plugins

import com.xentoryx.finance_tracker.presentation.dto.response.HealthErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*

val DatabaseReadyPlugin = createRouteScopedPlugin("DatabaseReadyPlugin") {
    onCall { call ->
        if (!isDatabaseReady) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                HealthErrorResponse(
                    status = "error",
                    message = "Database not ready, please wait..."
                )
            )
        }
    }
}