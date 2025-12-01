package com.thrive

import initDatabase
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    //ensure server can handle JSON responses / requests
    install(ContentNegotiation) {
        json()
    }
    // set up DB and REST routing for the application
    initDatabase()
    configureRouting()
}