#!/bin/bash

# Add these lines to your ~/.zshrc or ~/.bash_profile
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@17"' >> ~/.zshrc

# Reload shell configuration
source ~/.zshrc

# Verify Java installation
java -version
