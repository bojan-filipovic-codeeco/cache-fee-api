package com.alsoug.workflow.transaction.fee

import com.alsoug.transaction.TransactionService
import com.alsoug.workflow.transaction.dto.ComplianceRequest
import com.alsoug.workflow.transaction.dto.ComplianceResponse
import com.alsoug.workflow.transaction.dto.TransactionRequest
import com.alsoug.workflow.transaction.dto.TransactionResponse
import com.alsoug.workflow.transaction.fee.mapper.TransactionMapper
import dev.restate.common.Request
import dev.restate.common.Target
import dev.restate.sdk.annotation.Workflow
import dev.restate.sdk.kotlin.WorkflowContext
import dev.restate.sdk.kotlin.runBlock
import dev.restate.serde.kotlinx.jsonSerde
import org.slf4j.LoggerFactory

@Workflow
class FeeWorkflow(
    private val transactionService: TransactionService,
    private val feeService: FeeService
) {

    private val logger = LoggerFactory.getLogger(FeeWorkflow::class.java)

    @Workflow
    suspend fun run(ctx: WorkflowContext, request: TransactionRequest): TransactionResponse {
        logger.info("Starting saga workflow for transaction: ${request.transactionId}")

        // Step 1: Validate the transaction
        val isValid = ctx.runBlock {
            logger.info("Step 1: Validating transaction ${request.transactionId}")
            feeService.validateTransaction(request)
        }

        if (!isValid) {
            throw RuntimeException("Transaction validation failed for ${request.transactionId}")
        }

        // Step 2: Calculate the base fee
        val baseFee = ctx.runBlock {
            logger.info("Step 2: Calculating base fee for transaction ${request.transactionId}")
            feeService.calculateBaseFee(request)
        }

        // Step 3: Apply discounts
        val discountedFee = ctx.runBlock {
            logger.info("Step 3: Applying discounts for transaction ${request.transactionId}")
            feeService.applyDiscounts(request, baseFee)
        }

        // Step 4: Apply additional fees
        val finalFee = ctx.runBlock {
            logger.info("Step 4: Applying additional fees for transaction ${request.transactionId}")
            feeService.applyAdditionalFees(request, discountedFee)
        }

        // Step 5: Perform external compliance check using ctx.call and await
        logger.info("Step 5: Performing external compliance check for transaction ${request.transactionId}")

        // Create a ComplianceRequest object
        val complianceRequest = ComplianceRequest(request, finalFee)

        // Create a Request object for the ComplianceWorkflow.checkExternalComplianceWithRequest method
        val requestObj = Request.of<ComplianceRequest, ComplianceResponse>(
            Target.workflow("ComplianceWorkflow", request.transactionId, "checkExternalComplianceWithRequest"),
            jsonSerde<ComplianceRequest>(),
            jsonSerde<ComplianceResponse>(),
            complianceRequest
        )

        // Call the ComplianceWorkflow using ctx.call and await the result
        val complianceFuture = ctx.call(requestObj)
        val complianceResponse = complianceFuture.await()
        val isCompliant = complianceResponse.isCompliant

        if (!isCompliant) {
            throw RuntimeException("Compliance check failed for ${request.transactionId}")
        }

        // Step 6: Finalize fee calculation
        val response = ctx.runBlock {
            logger.info("Step 6: Finalizing fee calculation for transaction ${request.transactionId}")
            feeService.finalizeFeeCalculation(request, finalFee, 6)
        }

        // Step 7: Persist the final response as a Transaction
        ctx.runBlock {
            logger.info("Step 7: Persisting finalized transaction ${response.transactionId}")
            val transaction = TransactionMapper.fromResponse(request, response)
            transactionService.create(transaction)
        }

        return response
    }
}
