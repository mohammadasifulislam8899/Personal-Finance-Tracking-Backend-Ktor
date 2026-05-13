package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.usecase.category.*
import com.xentoryx.finance_tracker.presentation.dto.request.CreateCategoryRequest
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateCategoryRequest
import com.xentoryx.finance_tracker.presentation.dto.response.MessageResponse
import com.xentoryx.finance_tracker.presentation.mapper.toCategoryResponse
import com.xentoryx.finance_tracker.presentation.mapper.toCategoryResponseList
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.categoryRoutes() {

    val createCategoryUseCase by inject<CreateCategoryUseCase>()
    val getCategoriesUseCase  by inject<GetCategoriesUseCase>()
    val updateCategoryUseCase by inject<UpdateCategoryUseCase>()
    val deleteCategoryUseCase by inject<DeleteCategoryUseCase>()

    authenticate("auth-jwt") {
        route("/categories") {

            // POST /categories
            post {
                val userId = call.userId() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val req      = call.receive<CreateCategoryRequest>()
                val category = createCategoryUseCase(userId, req)
                call.respond(HttpStatusCode.Created, category.toCategoryResponse())
            }

            // GET /categories  (system + user এর নিজের)
            get {
                val userId     = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val categories = getCategoriesUseCase(userId)
                call.respond(categories.toCategoryResponseList())
            }

            // GET /categories/{id}
            get("/{id}") {
                val userId = call.userId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid category id")) }

                val category = getCategoriesUseCase(id, userId)
                call.respond(category.toCategoryResponse())
            }

            // PUT /categories/{id}
            put("/{id}") {
                val userId = call.userId() ?: return@put call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@put call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid category id")) }

                val req      = call.receive<UpdateCategoryRequest>()
                val category = updateCategoryUseCase(id, userId, req)
                call.respond(category.toCategoryResponse())
            }

            // DELETE /categories/{id}
            delete("/{id}") {
                val userId = call.userId() ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized, MessageResponse("Invalid token")
                )
                val id = runCatching { UUID.fromString(call.parameters["id"]) }
                    .getOrElse { return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid category id")) }

                deleteCategoryUseCase(id, userId)
                call.respond(MessageResponse("Category deleted successfully"))
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.userId(): UUID? {
    val raw = principal<JWTPrincipal>()
        ?.payload?.getClaim("userId")?.asString() ?: return null
    return runCatching { UUID.fromString(raw) }.getOrNull()
}