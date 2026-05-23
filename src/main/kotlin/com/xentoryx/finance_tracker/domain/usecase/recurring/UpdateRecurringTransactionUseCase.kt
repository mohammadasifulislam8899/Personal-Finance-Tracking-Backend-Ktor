package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringFrequency
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateRecurringTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class UpdateRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateRecurringTransactionRequest): RecurringTransaction {

        val existing = recurringRepository.findById(id)
            ?: throw NotFoundException("Recurring transaction not found")

        if (existing.userId != userId)
            throw NotFoundException("Recurring transaction not found")

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw ValidationException("Amount must be greater than zero")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException("Invalid type") }

        val frequency = runCatching { RecurringFrequency.valueOf(req.frequency.uppercase()) }
            .getOrElse { throw ValidationException("Invalid frequency") }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException("Invalid startDate format") }
        } ?: existing.startDate

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException("Invalid endDate format") }
        }

        return recurringRepository.update(
            existing.copy(
                accountId   = UUID.fromString(req.accountId),
                categoryId  = UUID.fromString(req.categoryId),
                amount      = amount,
                type        = type,
                frequency   = frequency,
                note        = req.note?.trim(),
                startDate   = startDate,
                endDate     = endDate
            )
        )
    }
}
