package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.budget.*
import com.xentoryx.finance_tracker.presentation.dto.request.CreateBudgetRequest
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateBudgetRequest
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import com.xentoryx.finance_tracker.presentation.mapper.toBudgetResponse
import com.xentoryx.finance_tracker.presentation.mapper.toBudgetResponseList
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.budgetRoutes() {

    val createBudgetUseCase by inject<CreateBudgetUseCase>()
    val getBudgetsUseCase   by inject<GetBudgetsUseCase>()
    val updateBudgetUseCase by inject<UpdateBudgetUseCase>()
    val deleteBudgetUseCase by inject<DeleteBudgetUseCase>()

    authenticate("auth-jwt") {
        route("/budgets") {

            // POST /budgets
            post {
                val userId = call.userId() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val req    = call.receive<CreateBudgetRequest>()
                val budget = createBudgetUseCase(userId, req)
                    .let { b ->
                        // BudgetWithProgress wrap à¦•à¦°à¦¤à§‡ à¦¹à¦¬à§‡ response à¦à¦° à¦œà¦¨à§à¦¯
                        getBudgetsUseCase(b.id, userId)
                    }
                call.respond(HttpStatusCode.Created, budget.toBudgetResponse())
            }

            // GET /budgets
            get {
                val userId  = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val budgets = getBudgetsUseCase(userId)
                call.respond(budgets.toBudgetResponseList())
            }

            // GET /budgets/{id}
            get("/{id}") {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid budget id")) }

                val budget = getBudgetsUseCase(id, userId)
                call.respond(budget.toBudgetResponse())
            }

            // PUT /budgets/{id}
            put("/{id}") {
                val userId = call.userId() ?: return@put call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@put call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid budget id")) }

                val req    = call.receive<UpdateBudgetRequest>()
                val budget = updateBudgetUseCase(id, userId, req)
                    .let { b -> getBudgetsUseCase(b.id, userId) }
                call.respond(budget.toBudgetResponse())
            }

            // DELETE /budgets/{id}
            delete("/{id}") {
                val userId = call.userId() ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid budget id")) }

                deleteBudgetUseCase(id, userId)
                call.respond(MessageResponse("Budget deleted successfully"))
            }
        }
    }
}

