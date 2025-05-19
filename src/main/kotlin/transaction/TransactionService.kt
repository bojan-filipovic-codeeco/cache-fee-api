package com.alsoug.transaction

import com.alsoug.transaction.db.TransactionRepository

class TransactionService(private val repository: TransactionRepository) {

    suspend fun getAll(): List<Transaction> =
        repository.findAll()

    suspend fun getById(id: String): Transaction? =
        repository.findById(id)

    suspend fun create(tx: Transaction): Transaction =
        repository.save(tx)

    suspend fun delete(id: String): Boolean =
        repository.delete(id)
}
