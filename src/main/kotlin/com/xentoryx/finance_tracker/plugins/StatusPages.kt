//package com.xentoryx.finance_tracker.plugins
//
//import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
//import io.ktor.http.HttpStatusCode
//import io.ktor.server.application.Application
//import io.ktor.server.application.install
//import io.ktor.server.plugins.statuspages.StatusPages
//import io.ktor.server.response.respond
//
//fun Application.configureStatusPages() {
//    install(StatusPages) {
//
//        exception<IllegalArgumentException> { call, cause ->
//            call.respond(
//                HttpStatusCode.BadRequest,
//                MessageResponse(cause.message ?: "Bad request")
//            )
//        }
//
//        exception<Throwable> { call, cause ->
//            call.respond(
//                HttpStatusCode.InternalServerError,
//                MessageResponse(cause.message ?: "Unexpected error occurred")
//            )
//        }
//
//        status(HttpStatusCode.NotFound) { call, status ->
//            call.respond(status, MessageResponse("Route not found"))
//        }
//
//        status(HttpStatusCode.Unauthorized) { call, status ->
//            call.respond(status, MessageResponse("Unauthorized"))
//        }
//    }
//}
