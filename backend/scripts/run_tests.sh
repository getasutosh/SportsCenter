#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status messages
print_status() {
    echo -e "${YELLOW}==>${NC} $1"
}

# Function to print error messages
print_error() {
    echo -e "${RED}Error:${NC} $1"
}

# Function to print success messages
print_success() {
    echo -e "${GREEN}Success:${NC} $1"
}

# Check if Clojure CLI is installed
if ! command -v clojure &> /dev/null; then
    print_error "Clojure CLI is not installed. Please install it first."
    echo "Visit https://clojure.org/guides/install_clojure for installation instructions."
    exit 1
fi

# Create test directories if they don't exist
mkdir -p test/resources

print_status "Setting up test environment..."

# Check if deps.edn exists
if [ ! -f "deps.edn" ]; then
    print_error "deps.edn not found in current directory!"
    exit 1
fi

# Update dependencies
print_status "Updating dependencies..."
clojure -P || {
    print_error "Failed to update dependencies"
    exit 1
}

# Setup test databases
print_status "Setting up test databases..."
if [ -f "./scripts/setup_test_dbs.sh" ]; then
    ./scripts/setup_test_dbs.sh || {
        print_error "Failed to setup test databases"
        exit 1
    }
else
    print_error "setup_test_dbs.sh script not found!"
    exit 1
fi

# Run the tests using Kaocha
print_status "Running tests..."
TEST_RESULT=0
clojure -M:test || TEST_RESULT=$?

# Check the test results
if [ $TEST_RESULT -eq 0 ]; then
    print_success "All tests passed! "
    exit 0
else
    print_error "Some tests failed! "
    exit 1
fi
