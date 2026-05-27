#!/bin/bash

echo "Stop services"

docker compose --env-file ./compose.env down

echo "Stopped"
