package com.alsoug

import com.alsoug.config.configureDatabases
import com.alsoug.config.restate.RestateServer
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()

    // Start Restate server only if enabled
    val restateEnabled = environment.config
        .propertyOrNull("restate.enabled")
        ?.getString()
        ?.toBoolean() ?: true

    if (restateEnabled) {
        RestateServer.start()
    }

    configureDatabases()
    configureHTTP()
    configureDocs()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}

