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

# Stop InfluxDB
print_status "Stopping InfluxDB..."
brew services stop influxdb

# Remove existing data and configs
print_status "Cleaning up existing InfluxDB data..."
rm -rf /opt/homebrew/var/lib/influxdb/*
rm -rf ~/.influxdbv2

# Start InfluxDB fresh
print_status "Starting InfluxDB..."
brew services start influxdb
sleep 5

# Setup initial config with default values
print_status "Setting up InfluxDB..."
influx setup \
  --org sports_center \
  --bucket sports_center \
  --username admin \
  --password admin123456 \
  --retention 0 \
  --force

if [ $? -eq 0 ]; then
    print_success "InfluxDB setup completed successfully!"
    
    # Create test bucket
    print_status "Creating test bucket..."
    influx bucket create \
      --name sports_center_test \
      --org sports_center \
      --retention 0
    
    if [ $? -eq 0 ]; then
        print_success "Test bucket created successfully!"
    else
        print_error "Failed to create test bucket"
        exit 1
    fi
else
    print_error "Failed to setup InfluxDB"
    exit 1
fi
