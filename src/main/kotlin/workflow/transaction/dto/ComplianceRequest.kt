package com.alsoug.workflow.transaction.dto

import kotlinx.serialization.Serializable

@Serializable
data class ComplianceRequest(
    val request: TransactionRequest,
    val fee: Double
)