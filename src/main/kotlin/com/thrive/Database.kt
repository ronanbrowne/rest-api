import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

object EventsTable : Table("events") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description")
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val location = varchar("location", 255).nullable()
    val attendees = text("attendees").nullable()
    override val primaryKey = PrimaryKey(id)
}

fun initDatabase(inMemory: Boolean = false) {
    val db = if (inMemory) {
        Database.connect(url = "jdbc:sqlite:file:test?mode=memory&cache=shared", driver = "org.sqlite.JDBC")
    } else {
        Database.connect(url = "jdbc:sqlite:./events.db", driver = "org.sqlite.JDBC")
    }
    transaction(db) {
        // Create the Events table if it doesn't exist
        SchemaUtils.create(EventsTable)
    }
}
