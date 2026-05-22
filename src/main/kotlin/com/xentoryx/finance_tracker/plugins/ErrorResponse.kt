package com.xentoryx.finance_tracker.plugins

import com.xentoryx.finance_tracker.exception.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val status: Int, val error: String, val message: String)

fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest,
                ErrorResponse(400, "Bad Request", cause.message ?: "Invalid input"))
        }

        exception<AuthenticationException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized,
                ErrorResponse(401, "Unauthorized", cause.message ?: "Unauthorized"))
        }

        exception<ForbiddenException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden,
                ErrorResponse(403, "Forbidden", cause.message ?: "Forbidden"))
        }

        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound,
                ErrorResponse(404, "Not Found", cause.message ?: "Resource not found"))
        }

        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict,
                ErrorResponse(409, "Conflict", cause.message ?: "Conflict"))
        }

        status(HttpStatusCode.NotFound) { call, status ->
            respondWithError(call, status, "Page Not Found",
                "Oops! The endpoint or page you are looking for does not exist.")
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            respondWithError(call, status, "Unauthorized",
                "You are not authorized to access this resource.")
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled Exception caught", cause)
            respondWithError(call, HttpStatusCode.InternalServerError,
                "Internal Server Error",
                "Something went wrong on our servers. Our engineers have been notified.")
        }
    }
}

private suspend fun respondWithError(
    call: ApplicationCall, statusCode: HttpStatusCode, title: String, message: String
) {
    val accept = call.request.header(HttpHeaders.Accept) ?: ""
    if (accept.contains("text/html")) {
        call.respondText(buildHtmlPage(statusCode.value, title, message),
            ContentType.Text.Html, statusCode)
    } else {
        call.respond(statusCode, ErrorResponse(statusCode.value, title, message))
    }
}

private fun buildHtmlPage(code: Int, title: String, message: String) = """
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Error $code - $title</title>
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<style>body{font-family:'Inter',sans-serif;}</style></head>
<body class="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 flex items-center justify-center p-4">
<div class="text-center">
<h1 class="text-9xl font-black text-white opacity-10">$code</h1>
<h2 class="text-2xl font-bold text-white mt-4">$title</h2>
<p class="text-slate-400 mt-2">$message</p>
</div></body></html>""".trimIndent()
