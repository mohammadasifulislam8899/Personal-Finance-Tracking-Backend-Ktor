package com.xentoryx.finance_tracker.presentation.routes

import com.xentoryx.finance_tracker.domain.model.RefreshToken as RefreshTokenModel
import com.xentoryx.finance_tracker.domain.repository.auth.RefreshTokenRepository
import com.xentoryx.finance_tracker.domain.usecase.auth.*
import com.xentoryx.finance_tracker.infrastructure.email.EmailService
import com.xentoryx.finance_tracker.presentation.dto.request.*
import com.xentoryx.finance_tracker.presentation.dto.response.*
import com.xentoryx.finance_tracker.presentation.mapper.toAuthResponse
import com.xentoryx.finance_tracker.presentation.mapper.toUserResponse
import com.xentoryx.finance_tracker.security.JwtService
import com.xentoryx.finance_tracker.utils.getOtpEmailTemplate
import com.xentoryx.finance_tracker.utils.getPasswordResetEmailTemplate
import com.xentoryx.finance_tracker.utils.hashSHA256
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import java.util.UUID

fun Route.authRoutes() {

    val registerUseCase by inject<RegisterUseCase>()
    val loginUseCase by inject<LoginUseCase>()
    val verifyEmailUseCase by inject<VerifyEmailUseCase>()
    val resendOtpUseCase by inject<ResendOtpUseCase>()
    val refreshTokenUseCase by inject<RefreshTokenUseCase>()
    val logoutUseCase by inject<LogoutUseCase>()
    val forgotPasswordUseCase by inject<ForgotPasswordUseCase>()
    val resetPasswordUseCase by inject<ResetPasswordUseCase>()
    val getUserProfileUseCase by inject<GetUserProfileUseCase>()
    val jwtService by inject<JwtService>()
    val refreshTokenRepository by inject<RefreshTokenRepository>()
    val emailService by inject<EmailService>()

    route("/auth") {

        post("/register") {
            try {
                val req = call.receive<RegisterRequest>()
                val (user, otp) = registerUseCase(req.email, req.password, req.fullName)
                call.application.launch {
                    emailService.sendHtmlEmail(user.email, "Verify Your OptiSpend Account", getOtpEmailTemplate(user.fullName, otp))
                }
                call.respond(HttpStatusCode.Created, RegisterResponse(user = user.toUserResponse(), message = "Registration successful. Please check your email for the OTP."))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Registration failed"))
            }
        }

        post("/login") {
            try {
                val req = call.receive<LoginRequest>()
                val user = loginUseCase(req.email, req.password)
                val rawAccessToken = jwtService.generateAccessToken(user.id.toString())
                val rawRefreshToken = jwtService.generateRefreshToken(user.id.toString())
                refreshTokenRepository.save(
                    RefreshTokenModel(
                        id = UUID.randomUUID(),
                        userId = user.id,
                        tokenHash = rawRefreshToken.hashSHA256(),
                        deviceInfo = call.request.headers["User-Agent"],
                        ipAddress = call.request.local.remoteHost,
                        expiresAt = LocalDateTime.now().plusDays(30),
                        revokedAt = null,
                        createdAt = LocalDateTime.now()
                    )
                )
                call.respond(toAuthResponse(user, rawAccessToken, rawRefreshToken))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse(e.message ?: "Login failed"))
            }
        }

        post("/verify-email") {
            try {
                val req = call.receive<VerifyEmailRequest>()
                verifyEmailUseCase(UUID.fromString(req.userId), req.otp)
                call.respond(MessageResponse("Email verified successfully"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Verification failed"))
            }
        }

        post("/resend-otp") {
            try {
                val req = call.receive<ResendOtpRequest>()
                val (user, newOtp) = resendOtpUseCase(req.email)
                call.application.launch {
                    emailService.sendHtmlEmail(user.email, "New Verification Code - OptiSpend", getOtpEmailTemplate(user.fullName, newOtp))
                }
                call.respond(HttpStatusCode.OK, MessageResponse("New OTP sent successfully"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Failed to resend OTP"))
            }
        }

        post("/refresh") {
            try {
                val req = call.receive<RefreshTokenRequest>()
                val newAccessToken = refreshTokenUseCase(req.refreshToken)
                call.respond(TokenResponse(accessToken = newAccessToken, refreshToken = req.refreshToken))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse(e.message ?: "Token refresh failed"))
            }
        }

        post("/forgot-password") {
            try {
                val req = call.receive<ForgotPasswordRequest>()
                val (rawToken, userEmail) = forgotPasswordUseCase(req.email)
                val name = req.email.substringBefore("@")
                call.application.launch {
                    emailService.sendHtmlEmail(userEmail, "Reset Your OptiSpend Password", getPasswordResetEmailTemplate(name, rawToken))
                }
                call.respond(MessageResponse("If this email exists, a reset link will be sent"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Failed"))
            }
        }

        post("/reset-password") {
            try {
                val req = call.receive<ResetPasswordRequest>()
                resetPasswordUseCase(req.token, req.newPassword)
                call.respond(MessageResponse("Password reset successfully"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Reset failed"))
            }
        }
        authenticate("auth-jwt") {
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid token"))
                    val user = getUserProfileUseCase(UUID.fromString(userId))
                    call.respond(user.toUserResponse())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, MessageResponse(e.message ?: "User not found"))
                }
            }

            post("/logout") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid token"))
                    logoutUseCase(UUID.fromString(userId))
                    call.respond(MessageResponse("Logged out successfully"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, MessageResponse("Logout failed"))
                }
            }
        }
    }


}


