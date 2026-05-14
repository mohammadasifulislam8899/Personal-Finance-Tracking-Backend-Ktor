package com.xentoryx.finance_tracker.domain.repository.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import java.time.LocalDate
import java.util.UUID

interface RecurringTransactionRepository {

    suspend fun create(recurring: RecurringTransaction): RecurringTransaction

    suspend fun findById(id: UUID): RecurringTransaction?

    suspend fun findAllByUserId(userId: UUID): List<RecurringTransaction>

    // Scheduler এর জন্য — আজকে বা আগের due গুলো
    suspend fun findDueToday(today: LocalDate): List<RecurringTransaction>

    suspend fun update(recurring: RecurringTransaction): RecurringTransaction

    suspend fun updateNextRunDate(id: UUID, nextRunDate: LocalDate)

    suspend fun deactivate(id: UUID, userId: UUID): Boolean

    suspend fun delete(id: UUID, userId: UUID): Boolean
}