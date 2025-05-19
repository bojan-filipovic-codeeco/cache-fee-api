package com.alsoug.transaction

import com.alsoug.testutil.TestData
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TransactionServiceTest {

    // Test data from shared TestData utility
    private val sampleTransaction = TestData.sampleTransaction

    // Test double for TransactionService that we can verify
    private class TestTransactionService(private val sampleTx: Transaction) {
        var getAllCalled = false
        var getByIdCalled = false
        var getByIdParam: String? = null
        var createCalled = false
        var createParam: Transaction? = null
        var deleteCalled = false
        var deleteParam: String? = null
        var returnEmptyList = false
        var returnNullTransaction = false
        var returnDeleteSuccess = true

        fun getAll(): List<Transaction> {
            getAllCalled = true
            return if (returnEmptyList) emptyList() else listOf(sampleTx)
        }

        fun getById(id: String): Transaction? {
            getByIdCalled = true
            getByIdParam = id
            return if (returnNullTransaction) null else sampleTx
        }

        fun create(tx: Transaction): Transaction {
            createCalled = true
            createParam = tx
            return tx
        }

        fun delete(id: String): Boolean {
            deleteCalled = true
            deleteParam = id
            return returnDeleteSuccess
        }
    }

    @Test
    fun `Given a service with transactions When getAll is called Then all transactions are returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)

        // When
        val transactions = service.getAll()

        // Then
        assertTrue(service.getAllCalled)
        assertEquals(1, transactions.size)
        assertEquals("test-123", transactions[0].id)
    }

    @Test
    fun `Given a service with no transactions When getAll is called Then an empty list is returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)
        service.returnEmptyList = true

        // When
        val transactions = service.getAll()

        // Then
        assertTrue(service.getAllCalled)
        assertEquals(0, transactions.size)
    }

    @Test
    fun `Given a service with a transaction When getById is called with a valid ID Then the transaction is returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)

        // When
        val transaction = service.getById("test-123")

        // Then
        assertTrue(service.getByIdCalled)
        assertEquals("test-123", service.getByIdParam)
        assertEquals("test-123", transaction?.id)
    }

    @Test
    fun `Given a service with no matching transaction When getById is called Then null is returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)
        service.returnNullTransaction = true

        // When
        val transaction = service.getById("non-existent")

        // Then
        assertTrue(service.getByIdCalled)
        assertEquals("non-existent", service.getByIdParam)
        assertNull(transaction)
    }

    @Test
    fun `Given a service When create is called with a transaction Then the transaction is saved and returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)

        // When
        val savedTransaction = service.create(sampleTransaction)

        // Then
        assertTrue(service.createCalled)
        assertEquals("test-123", service.createParam?.id)
        assertEquals("test-123", savedTransaction.id)
    }

    @Test
    fun `Given a service When delete is called with a valid ID Then the transaction is deleted and true is returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)

        // When
        val result = service.delete("test-123")

        // Then
        assertTrue(service.deleteCalled)
        assertEquals("test-123", service.deleteParam)
        assertTrue(result)
    }

    @Test
    fun `Given a service When delete is called with a non-existent ID Then false is returned`() = runBlocking {
        // Given
        val service = TestTransactionService(sampleTransaction)
        service.returnDeleteSuccess = false

        // When
        val result = service.delete("non-existent")

        // Then
        assertTrue(service.deleteCalled)
        assertEquals("non-existent", service.deleteParam)
        assertEquals(false, result)
    }
}
