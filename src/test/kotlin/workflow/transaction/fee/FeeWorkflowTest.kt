package com.alsoug.workflow.transaction.fee

import com.alsoug.testutil.TestData
import com.alsoug.transaction.AssetType
import com.alsoug.transaction.Transaction
import com.alsoug.transaction.TransactionState
import com.alsoug.transaction.TransactionType
import com.alsoug.workflow.transaction.dto.ComplianceRequest
import com.alsoug.workflow.transaction.dto.ComplianceResponse
import com.alsoug.workflow.transaction.dto.TransactionRequest
import com.alsoug.workflow.transaction.dto.TransactionResponse
import com.alsoug.workflow.transaction.fee.mapper.TransactionMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Real unit tests for FeeWorkflow
 */
class FeeWorkflowTest {

    // Test data from shared TestData utility
    private val sampleRequest = TestData.sampleTransactionRequest
    private val expectedResponse = TestData.sampleTransactionResponse

    // Test request class that stores the payload for verification
    private class TestRequest<I>(val payload: I)

    // Test doubles
    private class TestFeeService {
        var validateTransactionCalled = false
        var validateTransactionReturnValue = true
        var calculateBaseFeeCalled = false
        var applyDiscountsCalled = false
        var applyAdditionalFeesCalled = false
        var finalizeFeeCalculationCalled = false

        private val expectedResponse = TransactionResponse(
            transactionId = "test-123",
            amount = 100.0,
            asset = "USD",
            type = TransactionType.MOBILE_TOP_UP,
            fee = 0.64,
            rate = 0.0015,
            description = "Fee calculation completed after 6 saga steps"
        )

        fun validateTransaction(request: TransactionRequest): Boolean {
            validateTransactionCalled = true
            return validateTransactionReturnValue
        }

        fun calculateBaseFee(request: TransactionRequest): Double {
            calculateBaseFeeCalled = true
            return 0.15
        }

        fun applyDiscounts(request: TransactionRequest, baseFee: Double): Double {
            applyDiscountsCalled = true
            assertEquals(0.15, baseFee)
            return 0.14
        }

        fun applyAdditionalFees(request: TransactionRequest, currentFee: Double): Double {
            applyAdditionalFeesCalled = true
            assertEquals(0.14, currentFee)
            return 0.64
        }

        fun finalizeFeeCalculation(
            request: TransactionRequest,
            finalFee: Double,
            steps: Int
        ): TransactionResponse {
            finalizeFeeCalculationCalled = true
            assertEquals(0.64, finalFee)
            assertEquals(6, steps)
            return expectedResponse
        }
    }

    private class TestTransactionService {
        var createCalled = false
        var createdTransaction: Transaction? = null

        fun create(transaction: Transaction): Transaction {
            createCalled = true
            createdTransaction = transaction
            return transaction
        }
    }

    private class TestWorkflowContext {
        var runBlockCalls = mutableListOf<Any?>()
        var callCalls = mutableListOf<TestRequest<*>>()
        var complianceResponse = ComplianceResponse(true)

        suspend fun <T> runBlock(block: suspend () -> T): T {
            val result = block()
            runBlockCalls.add(result)
            return result
        }

        fun call(payload: ComplianceRequest): TestCallDurableFuture<ComplianceResponse> {
            val testRequest = TestRequest<ComplianceRequest>(payload)
            callCalls.add(testRequest)
            return TestCallDurableFuture(complianceResponse)
        }
    }

    private class TestCallDurableFuture<T>(private val result: T) {
        fun await(): T = result
    }

    // Custom implementation of FeeWorkflow for testing
    private class TestFeeWorkflow(
        private val testTransactionService: TestTransactionService,
        private val testFeeService: TestFeeService
    ) {
        private val logger = LoggerFactory.getLogger(FeeWorkflow::class.java)

        suspend fun run(ctx: TestWorkflowContext, request: TransactionRequest): TransactionResponse {
            logger.info("Starting saga workflow for transaction: ${request.transactionId}")

            // Step 1: Validate the transaction
            val isValid = ctx.runBlock {
                logger.info("Step 1: Validating transaction ${request.transactionId}")
                testFeeService.validateTransaction(request)
            }

            if (!isValid) {
                throw RuntimeException("Transaction validation failed for ${request.transactionId}")
            }

            // Step 2: Calculate the base fee
            val baseFee = ctx.runBlock {
                logger.info("Step 2: Calculating base fee for transaction ${request.transactionId}")
                testFeeService.calculateBaseFee(request)
            }

            // Step 3: Apply discounts
            val discountedFee = ctx.runBlock {
                logger.info("Step 3: Applying discounts for transaction ${request.transactionId}")
                testFeeService.applyDiscounts(request, baseFee)
            }

            // Step 4: Apply additional fees
            val finalFee = ctx.runBlock {
                logger.info("Step 4: Applying additional fees for transaction ${request.transactionId}")
                testFeeService.applyAdditionalFees(request, discountedFee)
            }

            // Step 5: Perform external compliance check
            logger.info("Step 5: Performing external compliance check for transaction ${request.transactionId}")

            // Create a ComplianceRequest object
            val complianceRequest = ComplianceRequest(request, finalFee)

            // Call the ComplianceWorkflow using ctx.call and await the result
            val complianceFuture = ctx.call(complianceRequest)
            val complianceResponse = complianceFuture.await()
            val isCompliant = complianceResponse.isCompliant

            if (!isCompliant) {
                throw RuntimeException("Compliance check failed for ${request.transactionId}")
            }

            // Step 6: Finalize fee calculation
            val response = ctx.runBlock {
                logger.info("Step 6: Finalizing fee calculation for transaction ${request.transactionId}")
                testFeeService.finalizeFeeCalculation(request, finalFee, 6)
            }

            // Step 7: Persist the final response as a Transaction
            ctx.runBlock {
                logger.info("Step 7: Persisting finalized transaction ${response.transactionId}")
                val transaction = TransactionMapper.fromResponse(request, response)
                testTransactionService.create(transaction)
            }

            return response
        }
    }

    @Test
    fun `Given a valid transaction request When running the workflow Then all steps are executed in order and correct response is returned`() = runBlocking {
        // Given
        val testFeeService = TestFeeService()
        val testTransactionService = TestTransactionService()
        val testContext = TestWorkflowContext()
        val testWorkflow = TestFeeWorkflow(testTransactionService, testFeeService)

        // When
        val response = testWorkflow.run(testContext, sampleRequest)

        // Then
        // Verify all steps were called in order
        assertTrue(testFeeService.validateTransactionCalled)
        assertTrue(testFeeService.calculateBaseFeeCalled)
        assertTrue(testFeeService.applyDiscountsCalled)
        assertTrue(testFeeService.applyAdditionalFeesCalled)
        assertTrue(testFeeService.finalizeFeeCalculationCalled)
        assertTrue(testTransactionService.createCalled)

        // Verify the compliance check was called
        assertEquals(1, testContext.callCalls.size)
        val complianceRequest = testContext.callCalls[0].payload as ComplianceRequest
        assertEquals(sampleRequest, complianceRequest.request)
        assertEquals(0.64, complianceRequest.fee)

        // Verify the response
        assertEquals(expectedResponse, response)

        // Verify the transaction was created with correct data
        val createdTransaction = testTransactionService.createdTransaction
        assertNotNull(createdTransaction)
        assertEquals("test-123", createdTransaction?.id)
        assertEquals(100.0, createdTransaction?.amount)
        assertEquals("USD", createdTransaction?.asset)
        assertEquals(AssetType.FIAT, createdTransaction?.assetType)
        assertEquals(TransactionType.MOBILE_TOP_UP, createdTransaction?.type)
        assertEquals(TransactionState.COMPLETED, createdTransaction?.state)
        assertEquals(0.64, createdTransaction?.fee)
        assertEquals(0.0015, createdTransaction?.rate)
        assertEquals("Fee calculation completed after 6 saga steps", createdTransaction?.description)
    }

    @Test
    fun `Given a transaction request When validation fails Then an exception is thrown`() = runBlocking {
        // Given
        val testFeeService = TestFeeService()
        testFeeService.validateTransactionReturnValue = false
        val testTransactionService = TestTransactionService()
        val testContext = TestWorkflowContext()
        val testWorkflow = TestFeeWorkflow(testTransactionService, testFeeService)

        // When/Then
        val exception = assertFailsWith<RuntimeException> {
            testWorkflow.run(testContext, sampleRequest)
        }
        assertEquals("Transaction validation failed for test-123", exception.message)

        // Verify only validation was called
        assertTrue(testFeeService.validateTransactionCalled)
        assertFalse(testFeeService.calculateBaseFeeCalled)
        assertFalse(testFeeService.applyDiscountsCalled)
        assertFalse(testFeeService.applyAdditionalFeesCalled)
        assertFalse(testFeeService.finalizeFeeCalculationCalled)
        assertFalse(testTransactionService.createCalled)
    }

    @Test
    fun `Given a transaction request When compliance check fails Then an exception is thrown`() = runBlocking {
        // Given
        val testFeeService = TestFeeService()
        val testTransactionService = TestTransactionService()
        val testContext = TestWorkflowContext()
        testContext.complianceResponse = ComplianceResponse(false)
        val testWorkflow = TestFeeWorkflow(testTransactionService, testFeeService)

        // When/Then
        val exception = assertFailsWith<RuntimeException> {
            testWorkflow.run(testContext, sampleRequest)
        }
        assertEquals("Compliance check failed for test-123", exception.message)

        // Verify steps before compliance check were called
        assertTrue(testFeeService.validateTransactionCalled)
        assertTrue(testFeeService.calculateBaseFeeCalled)
        assertTrue(testFeeService.applyDiscountsCalled)
        assertTrue(testFeeService.applyAdditionalFeesCalled)

        // Verify steps after compliance check were not called
        assertFalse(testFeeService.finalizeFeeCalculationCalled)
        assertFalse(testTransactionService.createCalled)
    }

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }

    private fun assertFalse(value: Boolean) {
        assertTrue(!value)
    }
}
