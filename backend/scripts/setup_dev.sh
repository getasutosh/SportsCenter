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
check_homebrew() {
    print_status "Checking for Homebrew..."
    if ! command -v brew &> /dev/null; then
        print_error "Homebrew not found. Installing Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    else
        print_success "Homebrew is installed"
    fi
}

# Install development tools
install_dev_tools() {
    print_status "Installing development tools..."
    
    # Install Java
    brew install openjdk@17
    sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    
    # Install Clojure and build tools
    brew install clojure/tools/clojure
    brew install leiningen
    
    # Install Node.js and npm for frontend development
    brew install node
    
    # Install development utilities
    brew install git
    brew install jq
    brew install wget
    brew install httpie
    
    print_success "Development tools installed successfully"
}

# Setup development environment
setup_dev_env() {
    print_status "Setting up development environment..."
    
    # Create necessary directories
    mkdir -p ~/.clojure
    mkdir -p ~/.m2
    
    # Configure Clojure CLI tools
    cat > ~/.clojure/deps.edn << EOF
{:mvn/repos {"central" {:url "https://repo1.maven.org/maven2/"}
             "clojars" {:url "https://repo.clojars.org/"}}
 :aliases
 {:dev {:extra-deps {org.clojure/tools.namespace {:mvn/version "1.4.4"}
                     criterium/criterium {:mvn/version "0.4.6"}}}
  :test {:extra-paths ["test"]
         :extra-deps {org.clojure/test.check {:mvn/version "1.1.1"}
                     lambdaisland/kaocha {:mvn/version "1.87.1366"}}}}}
EOF
    
    print_success "Development environment configured"
}

# Run database setup scripts
setup_databases() {
    print_status "Setting up databases..."
    
    # Get the script directory
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    
    # Make scripts executable
    chmod +x "$SCRIPT_DIR/setup_databases.sh"
    chmod +x "$SCRIPT_DIR/setup_test_dbs.sh"
    
    # Run database setup scripts
    "$SCRIPT_DIR/setup_databases.sh"
    "$SCRIPT_DIR/setup_test_dbs.sh"
    
    print_success "Databases setup complete"
}

# Main execution
main() {
    print_status "Starting development environment setup..."
    
    check_homebrew
    install_dev_tools
    setup_dev_env
    setup_databases
    
    print_success "Development environment setup complete!"
    print_status "You can now start developing the SportsCenter application"
}

# Run main function
main
