package com.thrive

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import initDatabase
import java.sql.DriverManager

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/hello").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }


    @Test
    fun testCreateEvent() = testApplication {
        // 1. Create a keep-alive connection to the shared in-memory DB.
        // This prevents SQLite from dropping the DB when the Exposed transaction finishes.
        val keepAliveConnection = DriverManager.getConnection("jdbc:sqlite:file:test?mode=memory&cache=shared")

        application {
            // Configure the module manually to use the in-memory database
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            // Pass true to use SQLite in-memory mode
            initDatabase(inMemory = true)
            configureRouting()
        }

        val response = client.post("/events") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "title": "Team Meeting",
                    "description": "Weekly sync",
                    "startTime": "2023-10-27T10:00:00",
                    "endTime": "2023-10-27T11:00:00",
                    "location": "Room 101",
                    "attendees": ["alice@example.com", "bob@example.com"]
                }
            """.trimIndent())
        }

        // Print the error details if the status is not what we expect
        if (response.status != HttpStatusCode.Created) {
            println("❌ Request Failed!")
            println("Status: ${response.status}")
            println("Response Body: ${response.bodyAsText()}")
        }

        assertEquals(HttpStatusCode.Created, response.status) //201 status code for created

        // 2. Close the keep-alive connection after the test
        keepAliveConnection.close()
    }


}