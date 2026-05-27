#!/bin/bash

echo "Docker Compose Start"

docker compose --env-file ./compose.env up --build -d

echo "All Services started"
