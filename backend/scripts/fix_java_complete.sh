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

# Stop services that might be using Java
print_status "Stopping services..."
brew services stop elastic/tap/elasticsearch-full
brew services stop neo4j

# Remove all Java installations
print_status "Removing existing Java installations..."
sudo rm -rf /Library/Java/JavaVirtualMachines/*
sudo rm -rf /System/Library/Frameworks/JavaVM.framework/Versions/*
brew uninstall --force openjdk
brew uninstall --force openjdk@17

# Clean up Homebrew
print_status "Cleaning up Homebrew..."
brew cleanup
brew doctor

# Install OpenJDK 17
print_status "Installing OpenJDK 17..."
brew install openjdk@17

# Create necessary directories
print_status "Setting up Java directories..."
sudo mkdir -p /Library/Java/JavaVirtualMachines
sudo mkdir -p /System/Library/Frameworks/JavaVM.framework/Versions

# Set up symlinks
print_status "Setting up Java symlinks..."
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Fix permissions
print_status "Fixing permissions..."
sudo chown -R $(whoami):admin /Library/Java/JavaVirtualMachines
sudo chmod -R 755 /Library/Java/JavaVirtualMachines

# Remove quarantine attributes
print_status "Removing quarantine attributes..."
sudo xattr -r -d com.apple.quarantine /Library/Java/JavaVirtualMachines/openjdk-17.jdk || true
sudo xattr -r -d com.apple.quarantine /opt/homebrew/opt/openjdk@17 || true
sudo xattr -r -d com.apple.quarantine /opt/homebrew/Cellar/openjdk@17 || true

# Set up environment variables
print_status "Setting up environment variables..."
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@17"' >> ~/.zshrc
source ~/.zshrc

# Verify Java installation
print_status "Verifying Java installation..."
java -version

if [ $? -eq 0 ]; then
    print_success "Java has been successfully installed!"
    print_status "Now reinstalling Elasticsearch..."
    
    # Reinstall Elasticsearch
    brew uninstall --force elastic/tap/elasticsearch-full
    brew install elastic/tap/elasticsearch-full
    
    # Remove quarantine from Elasticsearch
    sudo xattr -r -d com.apple.quarantine /opt/homebrew/opt/elasticsearch-full || true
    sudo xattr -r -d com.apple.quarantine /opt/homebrew/Cellar/elasticsearch-full || true
    
    # Start Elasticsearch
    brew services start elastic/tap/elasticsearch-full
    
    print_status "Waiting for Elasticsearch to start..."
    sleep 10
    
    if curl -s http://localhost:9200 > /dev/null; then
        print_success "Elasticsearch is running successfully!"
        curl -X GET "http://localhost:9200"
    else
        print_error "Elasticsearch failed to start. Check logs at: /opt/homebrew/var/log/elasticsearch/elasticsearch_asutosh.log"
    fi
else
    print_error "Java installation failed. Please check the error messages above."
fi
