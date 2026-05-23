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

                            val tx = createTransactionUseCase(userId, req)

                call.respond(HttpStatusCode.Created, tx.toTransactionResponse())
            }

                            val tx = updateTransactionUseCase(id, userId, req)

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


