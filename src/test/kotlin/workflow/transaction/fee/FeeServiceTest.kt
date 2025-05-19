package com.alsoug.workflow.transaction.fee

import com.alsoug.testutil.TestData
import com.alsoug.transaction.AssetType
import com.alsoug.transaction.TransactionState
import com.alsoug.transaction.TransactionType
import com.alsoug.workflow.transaction.dto.TransactionRequest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeeServiceTest {

    // Using helper function from shared TestData utility
    private fun createSampleRequest(
        transactionId: String = "test-123",
        amount: Double = 100.0,
        asset: String = "USD",
        assetType: AssetType = AssetType.FIAT,
        type: TransactionType = TransactionType.MOBILE_TOP_UP,
        state: TransactionState = TransactionState.SETTLED_PENDING_FEE,
        createdAt: String = "2023-01-01T12:00:00"
    ): TransactionRequest {
        return TestData.createTransactionRequest(
            transactionId = transactionId,
            amount = amount,
            asset = asset,
            assetType = assetType,
            type = type,
            state = state,
            createdAt = createdAt
        )
    }

    @Test
    fun `Given a transaction request When calculating fee Then correct fee is returned`() {
        // Given
        val feeService = FeeService()
        val request = createSampleRequest(amount = 100.0, type = TransactionType.MOBILE_TOP_UP)

        // When
        val response = feeService.calculateFee(request)

        // Then
        assertEquals(0.15, response.fee)
        assertEquals(0.0015, response.rate)
        assertEquals("Standard fee rate of 0.15%", response.description)
    }

    @Test
    fun `Given a transaction request When validating transaction Then validation succeeds after retries`() = runBlocking {
        // Given
        val feeService = FeeService()
        val request = createSampleRequest()

        // The executeWithRetries method in FeeService will fail for the first 2 attempts
        // and succeed on the 3rd attempt (when attempt > maxFailures)
        // We need to call the method multiple times to get past the retries

        // First call - will throw exception
        try {
            feeService.validateTransaction(request)
        } catch (e: RuntimeException) {
            // Expected exception on first attempt
        }

        // Second call - will throw exception
        try {
            feeService.validateTransaction(request)
        } catch (_: RuntimeException) {
            // Expected exception on second attempt
        }

        // Third call - should succeed
        val isValid = feeService.validateTransaction(request)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `Given a transaction request When calculating base fee Then correct base fee is returned`() = runBlocking {
        // Given
        val feeService = FeeService()
        val request = createSampleRequest(amount = 100.0, type = TransactionType.MOBILE_TOP_UP)

        // The executeWithRetries method in FeeService will fail for the first 2 attempts
        // and succeed on the 3rd attempt (when attempt > maxFailures)

        // First call - will throw exception
        try {
            feeService.calculateBaseFee(request)
        } catch (e: RuntimeException) {
            // Expected exception on first attempt
        }

        // Second call - will throw exception
        try {
            feeService.calculateBaseFee(request)
        } catch (_: RuntimeException) {
            // Expected exception on second attempt
        }

        // Third call - should succeed
        val baseFee = feeService.calculateBaseFee(request)

        // Then
        assertEquals(0.15, baseFee)
    }

    @Test
    fun `Given a base fee When applying discounts Then 10 percent discount is applied`() = runBlocking {
        // Given
        val feeService = FeeService()
        val request = createSampleRequest()
        val baseFee = 0.15

        // The executeWithRetries method in FeeService will fail for the first 2 attempts
        // and succeed on the 3rd attempt (when attempt > maxFailures)

        // First call - will throw exception
        try {
            feeService.applyDiscounts(request, baseFee)
        } catch (_: RuntimeException) {
            // Expected exception on first attempt
        }

        // Second call - will throw exception
        try {
            feeService.applyDiscounts(request, baseFee)
        } catch (_: RuntimeException) {
            // Expected exception on second attempt
        }

        // Third call - should succeed
        val discountedFee = feeService.applyDiscounts(request, baseFee)

        // Then
        assertEquals(0.14, discountedFee) // 0.15 - (0.15 * 0.1) = 0.135, rounded to 0.14
    }

    @Test
    fun `Given a discounted fee When applying additional fees Then 0_50 is added`() = runBlocking {
        // Given
        val feeService = FeeService()
        val request = createSampleRequest()
        val discountedFee = 0.14

        // The executeWithRetries method in FeeService will fail for the first 2 attempts
        // and succeed on the 3rd attempt (when attempt > maxFailures)

        // First call - will throw exception
        try {
            feeService.applyAdditionalFees(request, discountedFee)
        } catch (_: RuntimeException) {
            // Expected exception on first attempt
        }

        // Second call - will throw exception
        try {
            feeService.applyAdditionalFees(request, discountedFee)
        } catch (_: RuntimeException) {
            // Expected exception on second attempt
        }

        // Third call - should succeed
        val finalFee = feeService.applyAdditionalFees(request, discountedFee)

        // Then
        assertEquals(0.64, finalFee) // 0.14 + 0.50 = 0.64
    }

    @Test
    fun `Given a final fee When finalizing fee calculation Then correct response is returned`() = runBlocking {
        // Given
        val feeService = FeeService()
        val request = createSampleRequest(amount = 100.0, type = TransactionType.MOBILE_TOP_UP)
        val finalFee = 0.64
        val steps = 6

        // The executeWithRetries method in FeeService will fail for the first 2 attempts
        // and succeed on the 3rd attempt (when attempt > maxFailures)

        // First call - will throw exception
        try {
            feeService.finalizeFeeCalculation(request, finalFee, steps)
        } catch (_: RuntimeException) {
            // Expected exception on first attempt
        }

        // Second call - will throw exception
        try {
            feeService.finalizeFeeCalculation(request, finalFee, steps)
        } catch (_: RuntimeException) {
            // Expected exception on second attempt
        }

        // Third call - should succeed
        val response = feeService.finalizeFeeCalculation(request, finalFee, steps)

        // Then
        assertEquals("test-123", response.transactionId)
        assertEquals(100.0, response.amount)
        assertEquals("USD", response.asset)
        assertEquals(TransactionType.MOBILE_TOP_UP, response.type)
        assertEquals(0.64, response.fee)
        assertEquals(0.0015, response.rate)
        assertEquals("Fee calculation completed after 6 saga steps", response.description)
    }

    @Test
    fun `Given different transaction types When getting rate Then correct rates are returned`() {
        // Given
        val feeService = FeeService()
        val mobileTopUpRequest = createSampleRequest(type = TransactionType.MOBILE_TOP_UP)
        val bankTransferRequest = createSampleRequest(type = TransactionType.BANK_TRANSFER)
        val cashOutRequest = createSampleRequest(type = TransactionType.CASH_OUT)

        // When
        val mobileTopUpResponse = feeService.calculateFee(mobileTopUpRequest)
        val bankTransferResponse = feeService.calculateFee(bankTransferRequest)
        val cashOutResponse = feeService.calculateFee(cashOutRequest)

        // Then
        assertEquals(0.0015, mobileTopUpResponse.rate)
        assertEquals(0.0025, bankTransferResponse.rate)
        assertEquals(0.005, cashOutResponse.rate)
    }
}
