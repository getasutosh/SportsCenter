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

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    print_error "Homebrew is not installed. Please install it first."
    exit 1
fi

# Install databases if not present
print_status "Installing required databases..."

# PostgreSQL
if ! brew ls --versions postgresql@14 > /dev/null; then
    print_status "Installing PostgreSQL..."
    brew install postgresql@14
else
    print_status "PostgreSQL already installed"
fi

# MongoDB
if ! brew ls --versions mongodb-community > /dev/null; then
    print_status "Installing MongoDB..."
    brew install mongodb-community
else
    print_status "MongoDB already installed"
fi

# Redis
if ! brew ls --versions redis > /dev/null; then
    print_status "Installing Redis..."
    brew install redis
else
    print_status "Redis already installed"
fi

# InfluxDB
if ! brew ls --versions influxdb > /dev/null; then
    print_status "Installing InfluxDB..."
    brew install influxdb
else
    print_status "InfluxDB already installed"
fi

# Neo4j
if ! brew ls --versions neo4j > /dev/null; then
    print_status "Installing Neo4j..."
    brew install neo4j
else
    print_status "Neo4j already installed"
fi

# Stop all services first
print_status "Stopping all database services..."
brew services stop postgresql@14
brew services stop mongodb-community
brew services stop redis
brew services stop influxdb
brew services stop neo4j

# Start services
print_status "Starting database services..."

# PostgreSQL
print_status "Starting PostgreSQL..."
brew services start postgresql@14
sleep 2
createuser -s postgres 2>/dev/null || true
print_success "PostgreSQL started"

# MongoDB
print_status "Starting MongoDB..."
brew services start mongodb-community
sleep 2
print_success "MongoDB started"

# Redis
print_status "Starting Redis..."
brew services start redis
sleep 2
print_success "Redis started"

# InfluxDB
print_status "Starting InfluxDB..."
brew services start influxdb
sleep 2
# Setup initial organization and bucket if not already done
influx setup --org sports_center --bucket sports_center --username admin --password admin123 --force 2>/dev/null || true
print_success "InfluxDB started"

# Neo4j
print_status "Starting Neo4j..."
brew services start neo4j
sleep 2
print_success "Neo4j started"

# Verify all services
print_status "Verifying all services..."
brew services list

print_success "All database services have been installed and started!"
print_status "You can now run ./scripts/setup_test_dbs.sh to set up test databases"
