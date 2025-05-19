package com.alsoug.transaction.db

import com.alsoug.config.dbQuery
import com.alsoug.transaction.Transaction
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class TransactionRepository {

    suspend fun findAll(): List<Transaction> = dbQuery {
        TransactionTable.selectAll().map { it.toTransaction() }
    }

    suspend fun findById(id: String): Transaction? = dbQuery {
        TransactionTable.selectAll()
            .where { TransactionTable.id eq id }
            .map { it.toTransaction() }
            .singleOrNull()
    }

    suspend fun save(tx: Transaction): Transaction = dbQuery {
        TransactionTable.insert {
            it[id] = tx.id
            it[amount] = tx.amount
            it[asset] = tx.asset
            it[assetType] = tx.assetType.name
            it[type] = tx.type.name
            it[state] = tx.state.name
            it[createdAt] = tx.createdAt
            it[fee] = tx.fee
            it[rate] = tx.rate
            it[description] = tx.description
        }
        tx
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        TransactionTable.deleteWhere { TransactionTable.id eq id } > 0
    }

    private fun ResultRow.toTransaction(): Transaction =
        Transaction(
            id = this[TransactionTable.id],
            amount = this[TransactionTable.amount],
            asset = this[TransactionTable.asset],
            assetType = enumValueOf(this[TransactionTable.assetType]),
            type = enumValueOf(this[TransactionTable.type]),
            state = enumValueOf(this[TransactionTable.state]),
            createdAt = this[TransactionTable.createdAt],
            fee = this[TransactionTable.fee],
            rate = this[TransactionTable.rate],
            description = this[TransactionTable.description]
        )
}
