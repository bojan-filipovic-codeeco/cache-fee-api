package com.alsoug.workflow.transaction.fee.mapper

import com.alsoug.transaction.Transaction
import com.alsoug.transaction.TransactionState
import com.alsoug.workflow.transaction.dto.TransactionRequest
import com.alsoug.workflow.transaction.dto.TransactionResponse
import java.time.Instant

object TransactionMapper {

    fun fromResponse(
        request: TransactionRequest,
        response: TransactionResponse,
        state: TransactionState = TransactionState.COMPLETED,
        createdAt: String = Instant.now().toString()
    ): Transaction {
        return Transaction(
            id = response.transactionId,
            amount = response.amount,
            asset = response.asset,
            assetType = request.assetType,
            type = response.type,
            state = state,
            createdAt = createdAt,
            fee = response.fee,
            rate = response.rate,
            description = response.description
        )
    }
}
