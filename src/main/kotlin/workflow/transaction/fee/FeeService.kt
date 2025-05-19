package com.alsoug.workflow.transaction.fee

import com.alsoug.transaction.TransactionType
import com.alsoug.workflow.transaction.dto.TransactionRequest
import com.alsoug.workflow.transaction.dto.TransactionResponse
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class FeeService {

    private val logger = LoggerFactory.getLogger(FeeService::class.java)
    private val sagaStepCounters = ConcurrentHashMap<String, AtomicInteger>()

    fun calculateFee(request: TransactionRequest): TransactionResponse {
        val amount = request.amount
        val rate = getRate(request.type)
        val fee = roundTo2Decimals(amount * rate)
        val description = "Standard fee rate of ${rate * 100}%"

        return TransactionResponse(
            transactionId = request.transactionId,
            amount = amount,
            asset = request.asset,
            type = request.type,
            fee = fee,
            rate = rate,
            description = description
        )
    }

    suspend fun validateTransaction(request: TransactionRequest): Boolean {
        val step = "validateTransaction"
        return executeWithRetries(request.transactionId, step, 2) {
            logger.info("Transaction ${request.transactionId} validated successfully")
            true
        }
    }

    suspend fun calculateBaseFee(request: TransactionRequest): Double {
        val step = "calculateBaseFee"
        val amount = request.amount
        val rate = getRate(request.type)

        return executeWithRetries(request.transactionId, step, 2) {
            val baseFee = roundTo2Decimals(amount * rate)
            logger.info("Base fee calculated for ${request.transactionId}: $baseFee")
            baseFee
        }
    }

    suspend fun applyDiscounts(request: TransactionRequest, baseFee: Double): Double {
        val step = "applyDiscounts"
        return executeWithRetries(request.transactionId, step, 2) {
            val discount = baseFee * 0.1
            val discounted = roundTo2Decimals(baseFee - discount)
            logger.info("Discount for ${request.transactionId}: -$discount, discounted fee: $discounted")
            discounted
        }
    }

    suspend fun applyAdditionalFees(request: TransactionRequest, currentFee: Double): Double {
        val step = "applyAdditionalFees"
        return executeWithRetries(request.transactionId, step, 2) {
            val additional = 0.50
            val finalFee = roundTo2Decimals(currentFee + additional)
            logger.info("Additional fee for ${request.transactionId}: +$additional, final fee: $finalFee")
            finalFee
        }
    }

    suspend fun finalizeFeeCalculation(
        request: TransactionRequest,
        finalFee: Double,
        steps: Int
    ): TransactionResponse {
        val step = "finalizeFeeCalculation"
        val amount = request.amount
        val rate = getRate(request.type)

        return executeWithRetries(request.transactionId, step, 2) {
            val description = "Fee calculation completed after $steps saga steps"

            val response = TransactionResponse(
                transactionId = request.transactionId,
                amount = amount,
                asset = request.asset,
                type = request.type,
                fee = roundTo2Decimals(finalFee),
                rate = rate,
                description = description
            )

            logger.info("Final response for ${request.transactionId}: $response")
            response
        }
    }

    private fun getRate(type: TransactionType): Double = when (type) {
        TransactionType.MOBILE_TOP_UP -> 0.0015
        TransactionType.BANK_TRANSFER -> 0.0025
        TransactionType.CASH_OUT      -> 0.005
    }

    private fun roundTo2Decimals(value: Double): Double =
        String.format("%.2f", value).toDouble()

    private suspend fun <T> executeWithRetries(
        transactionId: String,
        step: String,
        maxFailures: Int,
        block: () -> T
    ): T {
        val key = "$transactionId:$step"
        val counter = sagaStepCounters.computeIfAbsent(key) { AtomicInteger(0) }
        val attempt = counter.incrementAndGet()

        logger.info("Step '$step' for $transactionId, attempt $attempt")
        delay(10) // Simulate some processing time
        logger.info("Wait done in '$step'")

        if (attempt <= maxFailures) {
            val msg = "Error in step '$step' for $transactionId (attempt $attempt)"
            logger.error(msg)
            throw RuntimeException(msg)
        }

        logger.info("Step '$step' succeeded for $transactionId")
        return block()
    }
}
