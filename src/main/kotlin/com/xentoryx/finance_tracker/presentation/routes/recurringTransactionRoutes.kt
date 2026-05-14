package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.recurring.*
import com.xentoryx.finance_tracker.presentation.dto.request.CreateRecurringTransactionRequest
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateRecurringTransactionRequest
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import com.xentoryx.finance_tracker.presentation.mapper.toRecurringTransactionResponse
import com.xentoryx.finance_tracker.presentation.mapper.toRecurringTransactionResponseList
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.recurringTransactionRoutes() {

    val createUseCase by inject<CreateRecurringTransactionUseCase>()
    val getUseCase    by inject<GetRecurringTransactionsUseCase>()
    val updateUseCase by inject<UpdateRecurringTransactionUseCase>()
    val deleteUseCase by inject<DeleteRecurringTransactionUseCase>()

    authenticate("auth-jwt") {
        route("/recurring-transactions") {

            // POST /recurring-transactions
            post {
                val userId = call.userId() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val req       = call.receive<CreateRecurringTransactionRequest>()
                val recurring = createUseCase(userId, req)
                call.respond(HttpStatusCode.Created, recurring.toRecurringTransactionResponse())
            }

            // GET /recurring-transactions
            get {
                val userId    = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val recurring = getUseCase(userId)
                call.respond(recurring.toRecurringTransactionResponseList())
            }

            // GET /recurring-transactions/{id}
            get("/{id}") {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid id")) }

                val recurring = getUseCase(id, userId)
                call.respond(recurring.toRecurringTransactionResponse())
            }

            // PUT /recurring-transactions/{id}
            put("/{id}") {
                val userId = call.userId() ?: return@put call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@put call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid id")) }

                val req       = call.receive<UpdateRecurringTransactionRequest>()
                val recurring = updateUseCase(id, userId, req)
                call.respond(recurring.toRecurringTransactionResponse())
            }

            // DELETE /recurring-transactions/{id}
            delete("/{id}") {
                val userId = call.userId() ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid id")) }

                deleteUseCase(id, userId)
                call.respond(MessageResponse("Recurring transaction deleted successfully"))
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.userId(): UUID? {
    val raw = principal<JWTPrincipal>()
        ?.payload?.getClaim("userId")?.asString() ?: return null
    return runCatching { UUID.fromString(raw) }.getOrNull()
}