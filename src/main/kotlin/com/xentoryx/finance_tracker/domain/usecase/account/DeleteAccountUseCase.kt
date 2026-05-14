package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import java.util.UUID

class DeleteAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = accountRepository.softDelete(id, userId)
        if (!deleted) throw IllegalArgumentException("Account not found")
    }
}