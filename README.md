# Calendar API

This project is a RESTful API for managing calendar events, built with Kotlin and Ktor. It provides basic CRUD (Create, Read, Update, Delete) functionality for events.

## Features

- **Create, Read, Update, and Delete (CRUD) operations for calendar events.**
- **SQLite database for data persistence.**
- **JSON serialization/deserialization for event data.**
- **A simple and clean API design.**

## API Endpoints

The following endpoints are available:

| Method | Path           | Description              |
|--------|----------------|--------------------------|
| `POST` | `/events`      | Creates a new event.     |
| `GET`  | `/events`      | Retrieves all events.    |
| `PUT`  | `/events/{id}` | Updates an event by ID.  |
| `DELETE`| `/events/{id}`| Deletes an event by ID.  |
| `GET`  | `/hello`       | A simple health-check endpoint. |

### Event Payload

The event payload for `POST` and `PUT` requests should be a JSON object with the following structure:

```json
{
    "title": "string",
    "description": "string",
    "startTime": "string (ISO 8601 format)",
    "endTime": "string (ISO 8601 format)",
    "location": "string",
    "attendees": ["string"]
}
```

## Building & Running

To build or run the project, use one of the following tasks:

| Task                | Description                               |
|---------------------|-------------------------------------------|
| `./gradlew test`    | Run the tests.                            |
| `./gradlew build`   | Build the project.                        |
| `./gradlew run`     | Run the server.                           |

The project includes a test script `test_app.sh` that you can use to test the API. This script will start the server, create, update, and delete events, and then stop the server.

To run the test script:
```bash
./test_app.sh
```
If the server starts successfully, you'll see the following output:
```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0._0.0.0:8080
```
The server will be available at `http://localhost:8080`.