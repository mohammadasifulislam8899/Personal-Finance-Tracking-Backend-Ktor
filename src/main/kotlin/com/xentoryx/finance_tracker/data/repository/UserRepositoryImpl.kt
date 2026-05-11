package com.xentoryx.finance_tracker.data.repository

import com.xentoryx.finance_tracker.data.mapper.toUser
import com.xentoryx.finance_tracker.data.table.Users
import com.xentoryx.finance_tracker.domain.model.User
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID

class UserRepositoryImpl(
    private val db: R2dbcDatabase
) : UserRepository {

    override suspend fun create(user: User): User {
        return suspendTransaction(db) {
            Users.insert {
                it[id] = user.id  // ✅ আমাদের UUID explicitly set
                it[email] = user.email
                it[passwordHash] = user.passwordHash
                it[fullName] = user.fullName
                it[currencyCode] = user.currencyCode
                it[isEmailVerified] = user.isEmailVerified
                it[isActive] = user.isActive
                it[createdAt] = user.createdAt
                it[updatedAt] = user.updatedAt
            }
            user
        }
    }

    override suspend fun findById(id: UUID): User? {
        return suspendTransaction(db) {
            Users.selectAll()
                .where { Users.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }
    }

    override suspend fun findByEmail(email: String): User? {
        return suspendTransaction(db) {
            Users.selectAll()
                .where { Users.email eq email }
                .map { it.toUser() }
                .singleOrNull()
        }
    }

    override suspend fun update(user: User): User {
        return suspendTransaction(db) {
            Users.update({ Users.id eq user.id }) {
                it[email] = user.email
                it[fullName] = user.fullName
                it[currencyCode] = user.currencyCode
                it[passwordHash] = user.passwordHash
                it[isEmailVerified] = user.isEmailVerified
                it[isActive] = user.isActive
                it[updatedAt] = user.updatedAt
            }
            user
        }
    }
    override suspend fun delete(id: UUID): Boolean {
        return suspendTransaction(db) {
            Users.deleteWhere { Users.id eq id } > 0
        }
    }
}
