package com.xentoryx.finance_tracker.scheduler

import com.xentoryx.finance_tracker.domain.usecase.recurring.ProcessRecurringTransactionsUseCase
import io.ktor.server.application.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private val logger = LoggerFactory.getLogger("RecurringScheduler")

fun Application.startRecurringScheduler() {
    val processUseCase by inject<ProcessRecurringTransactionsUseCase>()

    CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            try {
                val now            = LocalDateTime.now()
                val nextMidnight   = now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT)
                val millisUntilMidnight = ChronoUnit.MILLIS.between(now, nextMidnight)

                logger.info("Recurring scheduler sleeping ${millisUntilMidnight / 1000}s until midnight")
                delay(millisUntilMidnight)

                logger.info("Running recurring transaction processor...")
                processUseCase()

            } catch (e: Exception) {
                logger.error("Scheduler error: ${e.message}")
                delay(60_000) // error হলে ১ মিনিট wait করে retry
            }
        }
    }
}