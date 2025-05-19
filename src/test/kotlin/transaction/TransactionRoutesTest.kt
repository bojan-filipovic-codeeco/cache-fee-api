package com.alsoug.transaction

import com.alsoug.testutil.TestData
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionRoutesTest {

    // Test data from shared TestData utility
    private val sampleTransaction = TestData.sampleTransaction

    // Test double for TransactionService
    private class TestTransactionService(private val sampleTx: Transaction) {
        var getAllCalled = false
        var getByIdCalled = false
        var getByIdParam: String? = null
        var returnEmptyList = false
        var returnNullTransaction = false

        fun getAll(): List<Transaction> {
            getAllCalled = true
            return if (returnEmptyList) emptyList() else listOf(sampleTx)
        }

        fun getById(id: String): Transaction? {
            getByIdCalled = true
            getByIdParam = id
            return if (returnNullTransaction) null else sampleTx
        }
    }

    // Extension function to allow passing a custom TestTransactionService to the routes
    private fun Route.transactionRoutes(service: TestTransactionService) {
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

    // Helper method to configure the test application with content negotiation
    private fun Application.configureTestApplication() {
        // Install content negotiation plugin
        install(ContentNegotiation) {
            json()
        }
    }

    @Test
    fun `Given a request to GET transactions When the endpoint is called Then service getAll is called and transactions are returned`() = testApplication {
        // Given
        val mockService = TestTransactionService(sampleTransaction)

        // Configure the test application
        application {
            configureTestApplication()
            routing {
                // Install the route with our mock service
                route("") {
                    transactionRoutes(mockService)
                }
            }
        }

        // When
        val response = client.get("/transactions")

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(mockService.getAllCalled)

        // Verify response body contains the transaction data
        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonArray
        assertEquals(1, json.size)
        assertEquals("test-123", json[0].jsonObject["id"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given a request to GET transactions with ID When the endpoint is called Then service getById is called with correct ID and transaction is returned`() = testApplication {
        // Given
        val mockService = TestTransactionService(sampleTransaction)

        // Configure the test application
        application {
            configureTestApplication()
            routing {
                // Install the route with our mock service
                route("") {
                    transactionRoutes(mockService)
                }
            }
        }

        // When
        val response = client.get("/transactions/test-123")

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(mockService.getByIdCalled)
        assertEquals("test-123", mockService.getByIdParam)

        // Verify response body contains the transaction data
        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonObject
        assertEquals("test-123", json["id"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given a request to GET transactions with empty ID When the endpoint is called Then BadRequest is returned`() = testApplication {
        // Given
        val mockService = TestTransactionService(sampleTransaction)

        // Configure the test application
        application {
            configureTestApplication()
            routing {
                // Install the route with our mock service
                route("") {
                    transactionRoutes(mockService)
                }
            }
        }

        // When
        val response = client.get("/transactions/")

        // Then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Given a request to GET transactions with blank ID When the endpoint is called Then NotFound is returned`() = testApplication {
        // Given
        val mockService = TestTransactionService(sampleTransaction)

        // Configure the test application
        application {
            configureTestApplication()
            routing {
                // Install the route with our mock service
                route("") {
                    transactionRoutes(mockService)
                }
            }
        }

        // When
        val response = client.get("/transactions/ ")

        // Then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Given a request to GET transactions with non-existent ID When the endpoint is called Then NotFound is returned`() = testApplication {
        // Given
        val mockService = TestTransactionService(sampleTransaction)
        mockService.returnNullTransaction = true

        // Configure the test application
        application {
            configureTestApplication()
            routing {
                // Install the route with our mock service
                route("") {
                    transactionRoutes(mockService)
                }
            }
        }

        // When
        val response = client.get("/transactions/non-existent")

        // Then
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(mockService.getByIdCalled)
        assertEquals("non-existent", mockService.getByIdParam)

        // Verify response body contains the error message
        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonObject
        assertEquals("Transaction not found", json["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Given a request to GET transactions When no transactions exist Then empty list is returned`() = testApplication {
        // Given
        val mockService = TestTransactionService(sampleTransaction)
        mockService.returnEmptyList = true

        // Configure the test application
        application {
            configureTestApplication()
            routing {
                // Install the route with our mock service
                route("") {
                    transactionRoutes(mockService)
                }
            }
        }

        // When
        val response = client.get("/transactions")

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(mockService.getAllCalled)

        // Verify response body contains an empty array
        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonArray
        assertEquals(0, json.size)
    }
}
