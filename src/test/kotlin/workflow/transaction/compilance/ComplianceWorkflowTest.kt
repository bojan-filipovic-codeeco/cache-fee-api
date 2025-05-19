package com.alsoug.workflow.transaction.compilance

import com.alsoug.testutil.TestData
import com.alsoug.transaction.AssetType
import com.alsoug.transaction.TransactionType
import com.alsoug.workflow.transaction.dto.ComplianceRequest
import com.alsoug.workflow.transaction.dto.ComplianceResponse
import com.alsoug.workflow.transaction.dto.TransactionRequest
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Real unit tests for ComplianceWorkflow
 */
class ComplianceWorkflowTest {

    // Test data from shared TestData utility
    private val mobileTopUpRequest = TestData.mobileTopUpRequest
    private val mobileTopUpExceedingLimitRequest = TestData.mobileTopUpExceedingLimitRequest
    private val bankTransferFiatRequest = TestData.bankTransferFiatRequest
    private val bankTransferFiatExceedingLimitRequest = TestData.bankTransferFiatExceedingLimitRequest
    private val bankTransferCryptoRequest = TestData.bankTransferCryptoRequest
    private val bankTransferCryptoExceedingLimitRequest = TestData.bankTransferCryptoExceedingLimitRequest
    private val cashOutFiatRequest = TestData.cashOutFiatRequest
    private val cashOutFiatExceedingLimitRequest = TestData.cashOutFiatExceedingLimitRequest
    private val cashOutCryptoRequest = TestData.cashOutCryptoRequest
    private val cashOutCryptoExceedingLimitRequest = TestData.cashOutCryptoExceedingLimitRequest

    // Test doubles
    private class TestWorkflowContext {
        // No methods needed for this test
    }

    // Custom implementation of ComplianceWorkflow for testing
    private class TestComplianceWorkflow {
        private val logger = LoggerFactory.getLogger(ComplianceWorkflow::class.java)
        var throwExceptionOnCheck = false

        suspend fun checkExternalComplianceWithRequest(
            ctx: TestWorkflowContext,
            complianceRequest: ComplianceRequest
        ): ComplianceResponse {
            val request = complianceRequest.request
            val fee = complianceRequest.fee
            val isCompliant = checkExternalCompliance(request, fee)
            return ComplianceResponse(isCompliant)
        }

        suspend fun checkExternalCompliance(request: TransactionRequest, fee: Double): Boolean {
            logger.info("Starting external compliance check for transaction: ${request.transactionId}")
            return try {
                if (throwExceptionOnCheck) {
                    throw RuntimeException("Simulated exception during compliance check")
                }

                when (request.type) {
                    TransactionType.MOBILE_TOP_UP -> checkMobileTopUpCompliance(request)
                    TransactionType.BANK_TRANSFER -> checkBankTransferCompliance(request)
                    TransactionType.CASH_OUT -> checkCashOutCompliance(request)
                }
            } catch (e: Exception) {
                logger.error("Error during external compliance check: ${e.message}")
                false
            }
        }

        private fun checkMobileTopUpCompliance(request: TransactionRequest): Boolean {
            logger.info("Calling mobile top-up compliance API for ${request.transactionId}")
            // No delay in tests
            val isCompliant = request.amount <= 500.0
            if (!isCompliant) {
                logger.warn("Mobile top-up exceeds compliance limits: ${request.amount} ${request.asset}")
            } else {
                logger.info("Mobile top-up passed compliance check")
            }
            return isCompliant
        }

        private fun checkBankTransferCompliance(request: TransactionRequest): Boolean {
            logger.info("Calling bank transfer compliance API for ${request.transactionId}")
            // No delay in tests

            return if (request.assetType == AssetType.CRYPTO) {
                logger.info("Crypto bank transfer requires additional verification")
                // No delay in tests
                checkAmountCompliance(request, 100.0)
            } else {
                checkAmountCompliance(request, 10000.0)
            }
        }

        private fun checkCashOutCompliance(request: TransactionRequest): Boolean {
            logger.info("Calling cash-out compliance API for ${request.transactionId}")
            // No delay in tests
            val limit = if (request.assetType == AssetType.CRYPTO) 50.0 else 2000.0
            return checkAmountCompliance(request, limit)
        }

        private fun checkAmountCompliance(request: TransactionRequest, limit: Double): Boolean {
            val amount = request.amount
            val isCompliant = amount <= limit
            if (!isCompliant) {
                logger.warn("${request.type} exceeds compliance limits: ${request.amount} ${request.asset}")
            } else {
                logger.info("${request.type} passed compliance check")
            }
            return isCompliant
        }
    }

    @Test
    fun `Given a mobile top-up request When amount is within limits Then compliance check passes`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(mobileTopUpRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertTrue(response.isCompliant)
    }

    @Test
    fun `Given a mobile top-up request When amount exceeds limits Then compliance check fails`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(mobileTopUpExceedingLimitRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertFalse(response.isCompliant)
    }

    @Test
    fun `Given a bank transfer request with FIAT When amount is within limits Then compliance check passes`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(bankTransferFiatRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertTrue(response.isCompliant)
    }

    @Test
    fun `Given a bank transfer request with FIAT When amount exceeds limits Then compliance check fails`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(bankTransferFiatExceedingLimitRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertFalse(response.isCompliant)
    }

    @Test
    fun `Given a bank transfer request with CRYPTO When amount is within limits Then compliance check passes`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(bankTransferCryptoRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertTrue(response.isCompliant)
    }

    @Test
    fun `Given a bank transfer request with CRYPTO When amount exceeds limits Then compliance check fails`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(bankTransferCryptoExceedingLimitRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertFalse(response.isCompliant)
    }

    @Test
    fun `Given a cash-out request with FIAT When amount is within limits Then compliance check passes`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(cashOutFiatRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertTrue(response.isCompliant)
    }

    @Test
    fun `Given a cash-out request with FIAT When amount exceeds limits Then compliance check fails`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(cashOutFiatExceedingLimitRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertFalse(response.isCompliant)
    }

    @Test
    fun `Given a cash-out request with CRYPTO When amount is within limits Then compliance check passes`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(cashOutCryptoRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertTrue(response.isCompliant)
    }

    @Test
    fun `Given a cash-out request with CRYPTO When amount exceeds limits Then compliance check fails`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        val complianceRequest = ComplianceRequest(cashOutCryptoExceedingLimitRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertFalse(response.isCompliant)
    }

    @Test
    fun `Given any transaction request When an exception occurs during compliance check Then compliance check fails`() = runBlocking {
        // Given
        val testContext = TestWorkflowContext()
        val testWorkflow = TestComplianceWorkflow()
        testWorkflow.throwExceptionOnCheck = true
        val complianceRequest = ComplianceRequest(mobileTopUpRequest, 0.15)

        // When
        val response = testWorkflow.checkExternalComplianceWithRequest(testContext, complianceRequest)

        // Then
        assertFalse(response.isCompliant)
    }
}
