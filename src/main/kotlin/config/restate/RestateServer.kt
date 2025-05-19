package com.alsoug.config.restate

import com.alsoug.config.WORKFLOW_ENDPOINT_PORT
import com.alsoug.workflow.transaction.compilance.ComplianceWorkflow
import com.alsoug.workflow.transaction.fee.FeeWorkflow
import dev.restate.sdk.http.vertx.RestateHttpServer
import dev.restate.sdk.kotlin.endpoint.endpoint
import org.koin.core.context.GlobalContext

object RestateServer {
    fun start() {
        println("Starting workflow endpoint on port $WORKFLOW_ENDPOINT_PORT")

        // Retrieve Koin context and resolve services
        val koin = GlobalContext.get()
        val feeWorkflow = koin.get<FeeWorkflow>()
        val complianceWorkflow = koin.get<ComplianceWorkflow>()

        RestateHttpServer.listen(
            endpoint {
                // Use default serialization
                bind(feeWorkflow)
                bind(complianceWorkflow)
            },
            WORKFLOW_ENDPOINT_PORT
        )
    }
}
