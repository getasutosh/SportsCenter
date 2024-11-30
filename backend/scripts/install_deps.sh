#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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
    echo "Visit https://brew.sh for installation instructions."
    exit 1
fi

# Update Homebrew
print_status "Updating Homebrew..."
brew update

# Install PostgreSQL
print_status "Installing PostgreSQL..."
brew install postgresql@14
brew services start postgresql@14

# Install MongoDB
print_status "Installing MongoDB..."
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community

# Install Redis
print_status "Installing Redis..."
brew install redis
brew services start redis

# Install Elasticsearch
print_status "Installing Elasticsearch..."
brew tap elastic/tap
brew install elastic/tap/elasticsearch-full
brew services start elasticsearch-full

# Install InfluxDB and CLI
print_status "Installing InfluxDB..."
brew install influxdb
brew install influxdb-cli
brew services start influxdb

# Initialize InfluxDB with default organization and bucket
print_status "Setting up InfluxDB..."
if ! command -v influx &> /dev/null; then
    print_error "InfluxDB CLI installation failed"
    exit 1
fi

# Wait for InfluxDB to start
sleep 5

# Setup initial InfluxDB configuration
INFLUX_HOST="http://localhost:8086"
INFLUX_ORG="sports_center"
INFLUX_BUCKET="sports_center_test"
INFLUX_TOKEN="your-super-secret-auth-token"

influx setup --force \
    --host $INFLUX_HOST \
    --org $INFLUX_ORG \
    --bucket $INFLUX_BUCKET \
    --username admin \
    --password adminpassword \
    --token $INFLUX_TOKEN

# Install Neo4j
print_status "Installing Neo4j..."
brew install neo4j
brew services start neo4j

# Verify all services are running
print_status "Verifying services..."
brew services list | grep -E "postgresql|mongodb|redis|elasticsearch|influxdb|neo4j"

print_success "All dependencies installed successfully!"
echo "Note: Some services might need additional configuration."
echo "Please check the README.md for more details."

# Save InfluxDB configuration
print_status "Saving InfluxDB configuration..."
cat > /opt/homebrew/etc/influxdb2/config.yml << EOL
bolt-path: /opt/homebrew/var/influxdb2/influxd.bolt
engine-path: /opt/homebrew/var/influxdb2/engine
http-bind-address: :8086
query-memory-bytes: 1073741824
EOL

print_success "Configuration complete!"
