package com.xentoryx.finance_tracker.infrastructure.email

import io.ktor.server.application.ApplicationEnvironment
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class EmailService(private val environment: ApplicationEnvironment) {
    private val username = environment.config.property("email.username").getString()
    private val password = environment.config.property("email.password").getString()
    private val fromName = environment.config.property("email.fromName").getString()

    private val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
        put("mail.smtp.connectiontimeout", "5000")
        put("mail.smtp.timeout", "5000")
    }

    suspend fun sendHtmlEmail(toEmail: String, subject: String, htmlContent: String) = withContext(Dispatchers.IO) {
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
        })
        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username, fromName))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject, "UTF-8")
                setContent(htmlContent, "text/html; charset=utf-8")
            }
            Transport.send(message)
        } catch (e: Exception) {
            println("Email send failed: ${e.message}")
            e.printStackTrace()
        }
    }
}
