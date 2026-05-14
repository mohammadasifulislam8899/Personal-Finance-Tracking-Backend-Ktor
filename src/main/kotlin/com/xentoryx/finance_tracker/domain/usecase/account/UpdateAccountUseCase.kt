package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.model.AccountType
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateAccountRequest
import java.util.UUID

class UpdateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateAccountRequest): Account {

        if (req.name.isBlank())
            throw IllegalArgumentException("Account name cannot be empty")

        val type = runCatching { AccountType.valueOf(req.type.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid type. Must be BANK, CASH, MOBILE, CREDIT_CARD or OTHER") }

        val existing = accountRepository.findById(id)
            ?: throw IllegalArgumentException("Account not found")

        if (existing.userId != userId)
            throw IllegalArgumentException("Account not found")

        return accountRepository.update(
            existing.copy(
                name         = req.name.trim(),
                type         = type,
                currencyCode = req.currencyCode?.uppercase()?.trim() ?: existing.currencyCode
            )
        )
    }
}