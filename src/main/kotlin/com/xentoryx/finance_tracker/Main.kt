package com.xentoryx.finance_tracker

import com.xentoryx.finance_tracker.di.appModule
import com.xentoryx.finance_tracker.plugins.*
import com.xentoryx.finance_tracker.scheduler.startRecurringScheduler
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@module))
    }
    configureStatusPages()
    configureSerialization()
    configureSecurity()
    configureDatabases()
    configureRouting()
    configureLogging()
    startRecurringScheduler()
}