package com.alsoug

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            // Configure test environment with necessary properties
            config = MapApplicationConfig(
                "postgres.url" to "jdbc:postgresql://localhost:5432/cache-fee-api-db",
                "postgres.user" to "cache-fee-api-user",
                "postgres.password" to "cache-fee-api-password",
                "ktor.deployment.port" to "8765"
            )
        }
        application {
            module()
        }
        client.get("/transactions").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
