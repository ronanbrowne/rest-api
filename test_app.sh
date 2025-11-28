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
echo "Application Server is running."
    sleep 2

echo ""
echo -e "\033[32m--- POSTing test events ---\033[0m"
echo ""
POST_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{ "title": "My Script Event", "description": "An event created by the test script.", "startTime": "2025-12-03T10:00:00", "endTime": "2025-12-03T11:00:00", "location": "Script Location", "attendees": ["script_user_1@example.com", "script_user_2@example.com"] }' \
    http://localhost:8080/calendar)
echo "$POST_RESPONSE"
EVENT_ID=$(echo "$POST_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo "Created event with ID: $EVENT_ID"

echo ""
POST_RESPONSE2=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{ "title": "Second Script Event", "description": "Another event created by the test script.", "startTime": "2025-12-04T14:00:00", "endTime": "2025-12-04T15:00:00", "location": "Second Script Location", "attendees": ["script_user_3@example.com"] }' \
    http://localhost:8080/calendar)
echo "$POST_RESPONSE2"
EVENT_ID2=$(echo "$POST_RESPONSE2" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo "Created second event with ID: $EVENT_ID2"

echo ""
echo -e "\033[32m--- GETting all events ---\033[0m"
curl http://localhost:8080/calendar
echo "" # Newline for better output formatting

if [ -n "$EVENT_ID" ]; then
    echo ""
    echo -e "\033[32m--- PUTting an update to the event with ID: $EVENT_ID ---\033[0m"
    curl -X PUT \
         -H "Content-Type: application/json" \
         -d '{ "id": '$EVENT_ID', "title": "Updated Script Event", "description": "An event updated by the test script.", "startTime": "2025-12-04T10:00:00", "endTime": "2025-12-04T11:00:00", "location": "Updated Script Location", "attendees": ["updated_user@example.com"] }' \
         http://localhost:8080/calendar/$EVENT_ID
    echo "" # Newline for better output formatting

    echo ""
    echo -e "\033[32m--- GETting all events after update ---\033[0m"
    curl http://localhost:8080/calendar
    echo "" # Newline for better output formatting
else
    echo "ERROR: Could not extract event ID from POST response. Skipping PUT test."
fi

if [ -n "$EVENT_ID" ]; then
    echo ""
    echo -e "\033[32m--- DELETEing the event with ID: $EVENT_ID ---\033[0m"
    curl -X DELETE \
         http://localhost:8080/calendar/$EVENT_ID
    echo "" # Newline for better output formatting

    echo -e "\033[32m--- GETting all events after deletion ---\033[0m"
    curl http://localhost:8080/calendar
    echo "" # Newline for better output formatting
else
    echo "ERROR: Could not extract event ID from POST response. Skipping DELETE test."
fi

echo "Stopping the application..."
kill $APP_PID
wait $APP_PID 2>/dev/null
echo "Application stopped."

echo "Script finished."

