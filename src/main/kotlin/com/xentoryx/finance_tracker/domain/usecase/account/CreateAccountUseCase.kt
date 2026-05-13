package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.model.AccountType
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.presentation.dto.request.CreateAccountRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class CreateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateAccountRequest): Account {

        if (req.name.isBlank())
            throw IllegalArgumentException("Account name cannot be empty")

        val type = runCatching { AccountType.valueOf(req.type.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid type. Must be BANK, CASH, MOBILE, CREDIT_CARD or OTHER") }

        val balance = BigDecimal.valueOf(req.initialBalance ?: 0.0)
        if (balance < BigDecimal.ZERO)
            throw IllegalArgumentException("Initial balance cannot be negative")

        return accountRepository.create(
            Account(
                id           = UUID.randomUUID(),
                userId       = userId,
                name         = req.name.trim(),
                type         = type,
                balance      = balance,
                currencyCode = req.currencyCode?.uppercase()?.trim() ?: "BDT",
                isActive     = true,
                createdAt    = LocalDateTime.now()
            )
        )
    }
}