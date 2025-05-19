package com.alsoug.workflow.transaction.dto

import kotlinx.serialization.Serializable

@Serializable
data class ComplianceResponse(
    val isCompliant: Boolean
)