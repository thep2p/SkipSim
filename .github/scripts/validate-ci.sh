#!/bin/bash
# Script to validate CI setup locally before pushing

set -e  # Exit on any error

echo "🔍 Validating CI setup..."
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

echo "✅ Maven found: $(mvn --version | head -1)"
echo ""

# Check Java version
echo "📦 Java version:"
java -version
echo ""

# Clean any previous build artifacts
echo "🧹 Cleaning previous build artifacts..."
mvn clean
echo ""

# Download dependencies
echo "📥 Downloading dependencies..."
mvn dependency:resolve
echo ""

# Compile sources
echo "🔨 Compiling sources..."
mvn compile
echo ""

# Compile test sources
echo "🔨 Compiling test sources..."
mvn test-compile
echo ""

# Run tests
echo "🧪 Running tests..."
mvn test
echo ""

# Check test results
if [ $? -eq 0 ]; then
    echo "✅ All validation checks passed!"
    echo "✅ CI should work correctly"
    exit 0
else
    echo "❌ Validation failed"
    echo "❌ Please fix the issues before pushing"
    exit 1
fi
