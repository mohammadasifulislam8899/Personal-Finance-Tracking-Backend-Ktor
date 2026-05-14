package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringFrequency
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ProcessRecurringTransactionsUseCase(
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend operator fun invoke() {
        val today = LocalDate.now()
        val dueList = recurringRepository.findDueToday(today)

        logger.info("Processing ${dueList.size} recurring transactions for $today")

        dueList.forEach { recurring ->
            try {
                processOne(recurring, today)
            } catch (e: Exception) {
                logger.error("Failed to process recurring ${recurring.id}: ${e.message}")
            }
        }
    }

    private suspend fun processOne(recurring: RecurringTransaction, today: LocalDate) {

        // EndDate পেরিয়ে গেলে deactivate করো
        if (recurring.endDate != null && today.isAfter(recurring.endDate)) {
            recurringRepository.deactivate(recurring.id, recurring.userId)
            logger.info("Deactivated recurring ${recurring.id} — endDate passed")
            return
        }

        // Actual transaction create করো
        transactionRepository.create(
            Transaction(
                id                  = UUID.randomUUID(),
                userId              = recurring.userId,
                accountId           = recurring.accountId,
                categoryId          = recurring.categoryId,
                transferToAccountId = null,
                amount              = recurring.amount,
                type                = recurring.type,
                note                = recurring.note,
                transactionDate     = today,
                createdAt           = LocalDateTime.now()
            )
        )

        // nextRunDate update করো
        val nextRunDate = calculateNextRunDate(today, recurring.frequency)
        recurringRepository.updateNextRunDate(recurring.id, nextRunDate)

        logger.info("Processed recurring ${recurring.id} — next run: $nextRunDate")
    }

    private fun calculateNextRunDate(from: LocalDate, frequency: RecurringFrequency): LocalDate {
        return when (frequency) {
            RecurringFrequency.DAILY   -> from.plusDays(1)
            RecurringFrequency.WEEKLY  -> from.plusWeeks(1)
            RecurringFrequency.MONTHLY -> from.plusMonths(1)
            RecurringFrequency.YEARLY  -> from.plusYears(1)
        }
    }
}