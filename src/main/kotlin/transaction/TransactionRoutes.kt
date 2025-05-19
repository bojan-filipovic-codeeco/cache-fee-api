package com.alsoug.transaction

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.transactionRoutes() {
    val service by inject<TransactionService>()

    route("/transactions") {
        get {
            call.respond(service.getAll())
        }

        get("{id}") {
            val id = call.parameters["id"]?.takeIf { it.isNotBlank() }
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing or invalid ID")
                )

            val tx = service.getById(id)
            if (tx == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Transaction not found"))
            } else {
                call.respond(tx)
            }
        }
    }
}
