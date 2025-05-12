#!/bin/bash

# Set your actual values here
USER="myUsername"
PASS="myPassword"
HOST1="host1.example.com"
HOST2="host2.example.com"
HOST3="host3.example.com"

# Perform replacements in all JSON files
find . -type f -name "*.json" -exec sed -i \
-e "s|<user>|$USER|g" \
-e "s|<pass>|$PASS|g" \
-e "s|<host1>|$HOST1|g" \
-e "s|<host2>|$HOST2|g" \
-e "s|<host3>|$HOST3|g" {} +
