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

# Load environment variables
if [ -f .env.development ]; then
    export $(grep -v '^#' .env.development | xargs)
fi

# REPL startup
start_repl() {
    print_status "Starting Clojure REPL..."
    
    # Use Clojure CLI with dev and repl aliases
    clj -M:dev:repl
}

# Watch and reload namespace
watch_repl() {
    print_status "Starting REPL with watch and reload..."
    
    # Use tools.namespace for reloading
    clj -M:dev:repl:watch
}

# Show help
show_help() {
    echo "Usage: $0 [command]"
    echo
    echo "Commands:"
    echo "  start   Start standard REPL"
    echo "  watch   Start REPL with auto-reload"
    echo "  help    Show this help message"
}

# Main script logic
case "$1" in
    start)
        start_repl
        ;;
    watch)
        watch_repl
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        start_repl
        ;;
esac
