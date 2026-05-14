package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringFrequency
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.presentation.dto.request.CreateRecurringTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class CreateRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(
        userId: UUID,
        req: CreateRecurringTransactionRequest
    ): RecurringTransaction {

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw IllegalArgumentException("Amount must be greater than zero")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid type. Must be INCOME or EXPENSE") }

        if (type == TransactionType.TRANSFER)
            throw IllegalArgumentException("TRANSFER type not supported for recurring transactions")

        val frequency = runCatching { RecurringFrequency.valueOf(req.frequency.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid frequency. Must be DAILY, WEEKLY, MONTHLY or YEARLY") }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw IllegalArgumentException("Invalid startDate format. Use yyyy-MM-dd") }
        } ?: LocalDate.now()

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw IllegalArgumentException("Invalid endDate format. Use yyyy-MM-dd") }
        }

        if (endDate != null && startDate.isAfter(endDate))
            throw IllegalArgumentException("startDate must not be after endDate")

        return recurringRepository.create(
            RecurringTransaction(
                id          = UUID.randomUUID(),
                userId      = userId,
                accountId   = UUID.fromString(req.accountId),
                categoryId  = UUID.fromString(req.categoryId),
                amount      = amount,
                type        = type,
                frequency   = frequency,
                note        = req.note?.trim(),
                startDate   = startDate,
                endDate     = endDate,
                nextRunDate = startDate,
                isActive    = true,
                createdAt   = LocalDateTime.now()
            )
        )
    }
}