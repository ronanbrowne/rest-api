package com.thrive

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.deleteWhere
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


fun Application.configureRouting() {
    routing {
        get("/hello") {
            call.respondText(
                text = "Hello World!",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.OK
            )
        }

        // Get all events
        get("/events") {
            try {
                val events = transaction {
                    EventsTable.selectAll().map { rowToEvent(it) }
                }
                application.log.info("Found ${events.size} events")
                call.respond(events)
            } catch (e: Exception) {
                application.log.error("Error getting events", e)
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        // Create event
        post("/events"){
         try {
             val event = call.receive<Event>()
             val id = transaction {
                 EventsTable.insert {
                     it[title] = event.title
                     it[description] = event.description
                     it[startTime] = event.startTime
                     it[endTime] = event.endTime
                     it[location] = event.location
                     it[attendees] = Json.encodeToString(event.attendees)
                 } get EventsTable.id
             }
             val createdEvent = event.copy(id = id)
             call.respond(HttpStatusCode.Created, createdEvent)
         } catch (e: Exception) {
            application.log.error("Error creating event", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
         }
        }

        put("/events/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID format"))
                    return@put
                }
                val event = call.receive<Event>()
                val updatedRows = transaction {
                    EventsTable.update({ EventsTable.id eq id }) {
                        it[title] = event.title
                        it[description] = event.description
                        it[startTime] = event.startTime
                        it[endTime] = event.endTime
                        it[location] = event.location
                        it[attendees] = Json.encodeToString(event.attendees)
                    }
                }
                if (updatedRows > 0) {
                    call.respond(HttpStatusCode.OK, event.copy(id = id))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Event with ID $id not found"))
                }
            } catch (e: Exception) {
                application.log.error("Error updating event", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
            }
        }

        delete("/events/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID format"))
                    return@delete
                }
                val deletedRows = transaction {
                    EventsTable.deleteWhere { EventsTable.id eq id }
                }
                if (deletedRows > 0) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Event with ID $id not found"))
                }
            } catch (e: Exception) {
                application.log.error("Error deleting event", e)
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }


}
/**
 * Maps a [ResultRow] from the database to an [Event] object.
 *
 * @param row The database row containing event data.
 * @return An [Event] instance populated with values from the row.
 */
private fun rowToEvent(row: ResultRow) = Event(
    id = row[EventsTable.id],
    title = row[EventsTable.title],
    description = row[EventsTable.description],
    startTime = row[EventsTable.startTime],
    endTime = row[EventsTable.endTime],
    location = row[EventsTable.location], // Map the location field
    attendees = row[EventsTable.attendees]?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList() // Deserialize attendees
)
