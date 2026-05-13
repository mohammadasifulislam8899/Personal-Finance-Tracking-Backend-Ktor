package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.transaction.*
import com.xentoryx.finance_tracker.presentation.dto.request.CreateTransactionRequest
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateTransactionRequest
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import com.xentoryx.finance_tracker.presentation.dto.response.TransactionListResponse
import com.xentoryx.finance_tracker.presentation.mapper.toTransactionResponse
import com.xentoryx.finance_tracker.presentation.mapper.toTransactionResponseList
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.util.UUID

fun Route.transactionRoutes() {

    val createTransactionUseCase   by inject<CreateTransactionUseCase>()
    val getTransactionsUseCase     by inject<GetTransactionsUseCase>()
    val getTransactionByIdUseCase  by inject<GetTransactionByIdUseCase>()
    val updateTransactionUseCase   by inject<UpdateTransactionUseCase>()
    val deleteTransactionUseCase   by inject<DeleteTransactionUseCase>()

    authenticate("auth-jwt") {
        route("/transactions") {

            // POST /transactions
            post {
                val userId = call.userId()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                    )
                val req = call.receive<CreateTransactionRequest>()
                val tx  = createTransactionUseCase(userId, req)
                call.respond(HttpStatusCode.Created, tx.toTransactionResponse())
            }

            // GET /transactions?page=1&limit=20
            // GET /transactions?from=2025-05-01&to=2025-05-31
            get {
                val userId = call.userId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                    )

                val from  = call.request.queryParameters["from"]
                val to    = call.request.queryParameters["to"]
                val page  = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

                val list = if (from != null && to != null) {
                    val fromDate = runCatching { LocalDate.parse(from) }
                        .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid 'from' date")) }
                    val toDate = runCatching { LocalDate.parse(to) }
                        .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid 'to' date")) }
                    getTransactionsUseCase(userId, fromDate, toDate)
                } else {
                    getTransactionsUseCase(userId, page, limit)
                }

                call.respond(
                    TransactionListResponse(
                        data  = list.toTransactionResponseList(),
                        page  = page,
                        limit = limit,
                        total = list.size
                    )
                )
            }

            // GET /transactions/{id}
            get("/{id}") {
                val userId = call.userId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                    )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid transaction id")) }

                val tx = getTransactionByIdUseCase(id, userId)
                call.respond(tx.toTransactionResponse())
            }

            // PUT /transactions/{id}
            put("/{id}") {
                val userId = call.userId()
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                    )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@put call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid transaction id")) }

                val req = call.receive<UpdateTransactionRequest>()
                val tx  = updateTransactionUseCase(id, userId, req)
                call.respond(tx.toTransactionResponse())
            }

            // DELETE /transactions/{id}
            delete("/{id}") {
                val userId = call.userId()
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                    )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid transaction id")) }

                deleteTransactionUseCase(id, userId)
                call.respond(MessageResponse("Transaction deleted successfully"))
            }
        }
    }
}

// Extension — userId বার বার extract করার helper
private fun ApplicationCall.userId(): UUID? {
    val raw = principal<JWTPrincipal>()
        ?.payload?.getClaim("userId")?.asString() ?: return null
    return runCatching { UUID.fromString(raw) }.getOrNull()
}