package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import java.util.UUID

class GetAccountsUseCase(
    private val accountRepository: AccountRepository
) {
    // সব accounts
    suspend operator fun invoke(userId: UUID): List<Account> {
        return accountRepository.findAllByUserId(userId)
    }

    // Single account
    suspend operator fun invoke(id: UUID, userId: UUID): Account {
        val account = accountRepository.findById(id)
            ?: throw IllegalArgumentException("Account not found")

        if (account.userId != userId)
            throw IllegalArgumentException("Account not found")

        return account
    }
}