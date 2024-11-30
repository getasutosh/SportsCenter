#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
    echo -e "${YELLOW}==>${NC} $1"
}

print_error() {
    echo -e "${RED}Error:${NC} $1"
}

print_success() {
    echo -e "${GREEN}Success:${NC} $1"
}

# Function to check if a command exists
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 is not installed. Please install it first."
        exit 1
    fi
}

# Check required commands
print_status "Checking dependencies..."
check_command psql
check_command mongosh
check_command influx
check_command redis-cli

print_status "Setting up test databases..."

# PostgreSQL setup
print_status "Setting up PostgreSQL..."
dropdb -U $USER sports_center_test --if-exists
createdb -U $USER sports_center_test || {
    print_error "Failed to create PostgreSQL database"
    exit 1
}
print_success "PostgreSQL setup complete"

# MongoDB setup
print_status "Setting up MongoDB..."
mongosh --eval "db.dropDatabase()" sports_center_test || {
    print_error "Failed to setup MongoDB"
    exit 1
}
print_success "MongoDB setup complete"

# InfluxDB setup
print_status "Setting up InfluxDB..."
influx bucket delete -n sports_center_test -o sports_center 2>/dev/null
influx bucket create -n sports_center_test -o sports_center -r 7d || {
    print_error "Failed to setup InfluxDB"
    exit 1
}
print_success "InfluxDB setup complete"

# Redis setup
print_status "Setting up Redis..."
redis-cli FLUSHDB || {
    print_error "Failed to setup Redis"
    exit 1
}
print_success "Redis setup complete"

print_success "✅ All test databases setup complete!"
