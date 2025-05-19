package com.alsoug.config.restate

import com.alsoug.workflow.transaction.compilance.ComplianceWorkflow
import com.alsoug.workflow.transaction.fee.FeeService
import com.alsoug.workflow.transaction.fee.FeeWorkflow
import org.koin.dsl.module

val restateModule = module {
    single { FeeService() }           // Business logic
    single { ComplianceWorkflow() }    // Compliance service
    single { FeeWorkflow(get(), get()) }  // Inject both services into workflow
}
