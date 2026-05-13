package com.xentoryx.finance_tracker.domain.repository.account

import com.xentoryx.finance_tracker.domain.model.Account
import java.util.UUID

interface AccountRepository {

    suspend fun create(account: Account): Account

    suspend fun findById(id: UUID): Account?

    suspend fun findAllByUserId(userId: UUID): List<Account>

    suspend fun update(account: Account): Account

    suspend fun softDelete(id: UUID, userId: UUID): Boolean
}