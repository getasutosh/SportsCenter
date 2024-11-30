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

# Function to run tests
run_tests() {
    print_status "Running tests..."
    
    # Set up test databases if needed
    "$SCRIPT_DIR/setup_test_dbs.sh"
    
    if [ "$1" = "watch" ]; then
        print_status "Starting test watch mode..."
        clj -M:test -w
    elif [ -n "$1" ]; then
        print_status "Running test: $1"
        clj -M:test -v "$1"
    else
        print_status "Running all tests..."
        clj -M:test
    fi
}

# Function to reset test databases
reset_test_db() {
    print_status "Resetting test databases..."
    "$SCRIPT_DIR/setup_test_dbs.sh"
    print_success "Test databases reset successfully"
}

# Show help message
show_help() {
    echo "Usage: $0 [command]"
    echo
    echo "Commands:"
    echo "  (no args)  Run all tests"
    echo "  watch      Run tests in watch mode"
    echo "  reset-db   Reset test databases"
    echo "  help       Show this help message"
    echo "  <test>     Run specific test"
}

# Main script logic
case "$1" in
    watch)
        run_tests "watch"
        ;;
    reset-db)
        reset_test_db
        ;;
    help|--help|-h)
        show_help
        ;;
    "")
        run_tests
        ;;
    *)
        run_tests "$1"
        ;;
esac
