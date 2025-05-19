package com.alsoug.testutil

import com.alsoug.transaction.AssetType
import com.alsoug.transaction.Transaction
import com.alsoug.transaction.TransactionState
import com.alsoug.transaction.TransactionType
import com.alsoug.workflow.transaction.dto.TransactionRequest
import com.alsoug.workflow.transaction.dto.TransactionResponse

/**
 * Shared test data for unit tests.
 * This class provides common test data objects to reduce duplication across test classes.
 */
object TestData {

    /**
     * Sample Transaction object for tests
     */
    val sampleTransaction = Transaction(
        id = "test-123",
        amount = 100.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.MOBILE_TOP_UP,
        state = TransactionState.COMPLETED,
        createdAt = "2023-01-01T12:00:00",
        fee = 0.64,
        rate = 0.0015,
        description = "Test transaction"
    )

    /**
     * Sample TransactionRequest object for tests
     */
    val sampleTransactionRequest = TransactionRequest(
        transactionId = "test-123",
        amount = 100.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.MOBILE_TOP_UP,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Sample TransactionResponse object for tests
     */
    val sampleTransactionResponse = TransactionResponse(
        transactionId = "test-123",
        amount = 100.0,
        asset = "USD",
        type = TransactionType.MOBILE_TOP_UP,
        fee = 0.64,
        rate = 0.0015,
        description = "Fee calculation completed after 6 saga steps"
    )

    /**
     * Mobile top-up transaction request within limits
     */
    val mobileTopUpRequest = TransactionRequest(
        transactionId = "mobile-123",
        amount = 100.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.MOBILE_TOP_UP,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Mobile top-up transaction request exceeding limits
     */
    val mobileTopUpExceedingLimitRequest = TransactionRequest(
        transactionId = "mobile-456",
        amount = 600.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.MOBILE_TOP_UP,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Bank transfer FIAT transaction request within limits
     */
    val bankTransferFiatRequest = TransactionRequest(
        transactionId = "bank-fiat-123",
        amount = 5000.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.BANK_TRANSFER,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Bank transfer FIAT transaction request exceeding limits
     */
    val bankTransferFiatExceedingLimitRequest = TransactionRequest(
        transactionId = "bank-fiat-456",
        amount = 15000.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.BANK_TRANSFER,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Bank transfer CRYPTO transaction request within limits
     */
    val bankTransferCryptoRequest = TransactionRequest(
        transactionId = "bank-crypto-123",
        amount = 50.0,
        asset = "BTC",
        assetType = AssetType.CRYPTO,
        type = TransactionType.BANK_TRANSFER,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Bank transfer CRYPTO transaction request exceeding limits
     */
    val bankTransferCryptoExceedingLimitRequest = TransactionRequest(
        transactionId = "bank-crypto-456",
        amount = 150.0,
        asset = "BTC",
        assetType = AssetType.CRYPTO,
        type = TransactionType.BANK_TRANSFER,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Cash out FIAT transaction request within limits
     */
    val cashOutFiatRequest = TransactionRequest(
        transactionId = "cash-fiat-123",
        amount = 1000.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.CASH_OUT,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Cash out FIAT transaction request exceeding limits
     */
    val cashOutFiatExceedingLimitRequest = TransactionRequest(
        transactionId = "cash-fiat-456",
        amount = 3000.0,
        asset = "USD",
        assetType = AssetType.FIAT,
        type = TransactionType.CASH_OUT,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Cash out CRYPTO transaction request within limits
     */
    val cashOutCryptoRequest = TransactionRequest(
        transactionId = "cash-crypto-123",
        amount = 25.0,
        asset = "BTC",
        assetType = AssetType.CRYPTO,
        type = TransactionType.CASH_OUT,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Cash out CRYPTO transaction request exceeding limits
     */
    val cashOutCryptoExceedingLimitRequest = TransactionRequest(
        transactionId = "cash-crypto-456",
        amount = 75.0,
        asset = "BTC",
        assetType = AssetType.CRYPTO,
        type = TransactionType.CASH_OUT,
        state = TransactionState.SETTLED_PENDING_FEE,
        createdAt = "2023-01-01T12:00:00"
    )

    /**
     * Helper function to create a custom TransactionRequest with default values
     */
    fun createTransactionRequest(
        transactionId: String = "test-123",
        amount: Double = 100.0,
        asset: String = "USD",
        assetType: AssetType = AssetType.FIAT,
        type: TransactionType = TransactionType.MOBILE_TOP_UP,
        state: TransactionState = TransactionState.SETTLED_PENDING_FEE,
        createdAt: String = "2023-01-01T12:00:00"
    ): TransactionRequest {
        return TransactionRequest(
            transactionId = transactionId,
            amount = amount,
            asset = asset,
            assetType = assetType,
            type = type,
            state = state,
            createdAt = createdAt
        )
    }
}
