package com.alsoug.workflow.transaction.compilance

import com.alsoug.transaction.AssetType
import com.alsoug.transaction.TransactionType
import com.alsoug.workflow.transaction.dto.ComplianceRequest
import com.alsoug.workflow.transaction.dto.ComplianceResponse
import com.alsoug.workflow.transaction.dto.TransactionRequest
import dev.restate.sdk.annotation.Workflow
import dev.restate.sdk.kotlin.WorkflowContext
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Workflow
class ComplianceWorkflow {

    private val logger = LoggerFactory.getLogger(ComplianceWorkflow::class.java)

    @Workflow
    suspend fun checkExternalComplianceWithRequest(
        ctx: WorkflowContext,
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

    private suspend fun checkMobileTopUpCompliance(request: TransactionRequest): Boolean {
        logger.info("Calling mobile top-up compliance API for ${request.transactionId}")
        delay(3000)
        val isCompliant = request.amount.toBigDecimal() <= BigDecimal("500")
        if (!isCompliant) {
            logger.warn("Mobile top-up exceeds compliance limits: ${request.amount} ${request.asset}")
        } else {
            logger.info("Mobile top-up passed compliance check")
        }
        return isCompliant
    }

    private suspend fun checkBankTransferCompliance(request: TransactionRequest): Boolean {
        logger.info("Calling bank transfer compliance API for ${request.transactionId}")
        delay(2500)

        return if (request.assetType == AssetType.CRYPTO) {
            logger.info("Crypto bank transfer requires additional verification")
            delay(500)
            checkAmountCompliance(request, BigDecimal("100"))
        } else {
            checkAmountCompliance(request, BigDecimal("10000"))
        }
    }

    private suspend fun checkCashOutCompliance(request: TransactionRequest): Boolean {
        logger.info("Calling cash-out compliance API for ${request.transactionId}")
        delay(2000)
        val limit = if (request.assetType == AssetType.CRYPTO) BigDecimal("50") else BigDecimal("2000")
        return checkAmountCompliance(request, limit)
    }

    private fun checkAmountCompliance(request: TransactionRequest, limit: BigDecimal): Boolean {
        val amount = request.amount.toBigDecimal()
        val isCompliant = amount <= limit
        if (!isCompliant) {
            logger.warn("${request.type} exceeds compliance limits: ${request.amount} ${request.asset}")
        } else {
            logger.info("${request.type} passed compliance check")
        }
        return isCompliant
    }
}

