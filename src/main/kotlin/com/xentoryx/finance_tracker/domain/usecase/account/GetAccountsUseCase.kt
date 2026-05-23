package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetAccountsUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(userId: UUID): List<Account> {
        return accountRepository.findAllByUserId(userId)
    }

    suspend operator fun invoke(id: UUID, userId: UUID): Account {
        val account = accountRepository.findById(id)
            ?: throw NotFoundException("Account not found")

        if (account.userId != userId)
            throw NotFoundException("Account not found")

        return account
    }
}
