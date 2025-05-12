#!/bin/bash

# Set your actual values here
HOST1="host1.example.com"
HOST2="host2.example.com"
HOST3="host3.example.com"

# Perform replacements in all JSON files
find . -type f -name "*.json" -exec sed -i \
-e "s|<host1>|$HOST1|g" \
-e "s|<host2>|$HOST2|g" \
-e "s|<host3>|$HOST3|g" {} +
