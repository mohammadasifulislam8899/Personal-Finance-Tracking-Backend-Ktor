package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.account.*
import com.xentoryx.finance_tracker.presentation.dto.request.CreateAccountRequest
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateAccountRequest
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import com.xentoryx.finance_tracker.presentation.mapper.toAccountResponse
import com.xentoryx.finance_tracker.presentation.mapper.toAccountResponseList
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.accountRoutes() {

    val createAccountUseCase by inject<CreateAccountUseCase>()
    val getAccountsUseCase   by inject<GetAccountsUseCase>()
    val updateAccountUseCase by inject<UpdateAccountUseCase>()
    val deleteAccountUseCase by inject<DeleteAccountUseCase>()

    authenticate("auth-jwt") {
        route("/accounts") {

            // POST /accounts
            post {
                val userId = call.userId() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val req     = call.receive<CreateAccountRequest>()
                val account = createAccountUseCase(userId, req)
                call.respond(HttpStatusCode.Created, account.toAccountResponse())
            }

            // GET /accounts
            get {
                val userId   = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val accounts = getAccountsUseCase(userId)
                call.respond(accounts.toAccountResponseList())
            }

            // GET /accounts/{id}
            get("/{id}") {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid account id")) }

                val account = getAccountsUseCase(id, userId)
                call.respond(account.toAccountResponse())
            }

            // PUT /accounts/{id}
            put("/{id}") {
                val userId = call.userId() ?: return@put call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@put call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid account id")) }

                val req     = call.receive<UpdateAccountRequest>()
                val account = updateAccountUseCase(id, userId, req)
                call.respond(account.toAccountResponse())
            }

            // DELETE /accounts/{id}
            delete("/{id}") {
                val userId = call.userId() ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid account id")) }

                deleteAccountUseCase(id, userId)
                call.respond(MessageResponse("Account deleted successfully"))
            }
        }
    }
}

