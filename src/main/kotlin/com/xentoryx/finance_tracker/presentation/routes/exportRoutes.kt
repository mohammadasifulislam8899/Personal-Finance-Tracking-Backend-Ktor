package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.export.ExportTransactionsPdfUseCase
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

fun Route.exportRoutes() {

    val exportPdfUseCase by inject<ExportTransactionsPdfUseCase>()

    authenticate("auth-jwt") {
        route("/export") {

            // GET /export/pdf?from=2025-05-01&to=2025-05-31
            get("/pdf") {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )

                val fromStr = call.request.queryParameters["from"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("'from' query param required. Format: yyyy-MM-dd")
                    )

                val toStr = call.request.queryParameters["to"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("'to' query param required. Format: yyyy-MM-dd")
                    )

                val from = runCatching { LocalDate.parse(fromStr) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid 'from' date")) }

                val to = runCatching { LocalDate.parse(toStr) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid 'to' date")) }

                val pdfBytes = exportPdfUseCase(userId, from, to)

                val filename = "transactions_${fromStr}_${toStr}.pdf"

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName, filename
                    ).toString()
                )

                call.respondBytes(
                    bytes       = pdfBytes,
                    contentType = ContentType.Application.Pdf,
                    status      = HttpStatusCode.OK
                )
            }
        }
    }
}

