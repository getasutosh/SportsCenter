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

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Function to start all services
start_services() {
    print_status "Starting development services..."
    
    # Start PostgreSQL
    brew services start postgresql
    
    # Start MongoDB
    brew services start mongodb-community
    
    # Start Redis
    brew services start redis
    
    # Start InfluxDB
    brew services start influxdb
    
    print_success "All services started"
    
    # Show status
    show_status
}

# Function to stop all services
stop_services() {
    print_status "Stopping development services..."
    
    # Stop PostgreSQL
    brew services stop postgresql
    
    # Stop MongoDB
    brew services stop mongodb-community
    
    # Stop Redis
    brew services stop redis
    
    # Stop InfluxDB
    brew services stop influxdb
    
    print_success "All services stopped"
}

# Function to show service status
show_status() {
    print_status "Checking service status..."
    brew services list | grep -E 'postgresql|mongodb-community|redis|influxdb'
}

# Function to view logs
view_logs() {
    if [ -z "$1" ]; then
        print_error "Please specify a service name (postgresql, mongodb, redis, influxdb)"
        exit 1
    fi
    
    case $1 in
        postgresql)
            tail -f /opt/homebrew/var/log/postgresql.log
            ;;
        mongodb)
            tail -f /opt/homebrew/var/log/mongodb/mongo.log
            ;;
        redis)
            tail -f /opt/homebrew/var/log/redis.log
            ;;
        influxdb)
            tail -f /opt/homebrew/var/log/influxdb/influxd.log
            ;;
        *)
            print_error "Unknown service: $1"
            exit 1
            ;;
    esac
}

# Function to reset databases
reset_db() {
    print_status "Resetting development databases..."
    
    # Stop services first
    stop_services
    
    # Clean up database files
    rm -rf /opt/homebrew/var/postgresql/*
    rm -rf /opt/homebrew/var/mongodb/*
    rm -rf /opt/homebrew/var/redis/*
    rm -rf /opt/homebrew/var/influxdb/*
    
    # Start services
    start_services
    
    # Run database setup
    "$SCRIPT_DIR/setup_databases.sh"
    
    print_success "Development databases reset successfully"
}

# Function to reset entire development environment
reset_env() {
    print_status "Resetting development environment..."
    
    # Stop all services
    stop_services
    
    # Run setup script again
    "$SCRIPT_DIR/setup_dev.sh"
    
    print_success "Development environment reset successfully"
}

# Show help message
show_help() {
    echo "Usage: $0 [command]"
    echo
    echo "Commands:"
    echo "  start      Start all development services"
    echo "  stop       Stop all development services"
    echo "  status     Show service status"
    echo "  logs       View logs (specify service: postgresql, mongodb, redis, influxdb)"
    echo "  reset-db   Reset development databases"
    echo "  reset      Reset entire development environment"
    echo "  help       Show this help message"
}

# Main script logic
case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    status)
        show_status
        ;;
    logs)
        view_logs "$2"
        ;;
    reset-db)
        reset_db
        ;;
    reset)
        reset_env
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
