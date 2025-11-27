#!/bin/bash

# Ensure port 8080 is free before starting
echo "Checking for running application on port 8080 and stopping it if found..."
kill $(lsof -t -i:8080) 2>/dev/null || true
sleep 1 # Give the OS a moment to release the port

# Ensure a clean state by deleting the database file
rm -f events.db

echo "Cleaning the build..."
./gradlew clean

echo "Starting the application in the background..."
./gradlew run > app.log 2>&1 &
APP_PID=$!

echo "Waiting for the application to start..."
until curl -s http://localhost:8080/hello > /dev/null; do
    sleep 3
done
echo "Application is running."
    sleep 2
echo "--- POSTing a test event ---"
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{ "title": "My Script Event", "description": "An event created by the test script.", "startTime": "2025-12-03T10:00:00", "endTime": "2025-12-03T11:00:00", "location": "Script Location", "attendees": ["script_user_1@example.com", "script_user_2@example.com"] }' \
     http://localhost:8080/events
echo "" # Newline for better output formatting

echo "--- GETting all events ---"
curl http://localhost:8080/events
echo "" # Newline for better output formatting

echo "Stopping the application..."
kill $APP_PID
wait $APP_PID 2>/dev/null
echo "Application stopped."

echo "Script finished."

