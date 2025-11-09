# Makefile for SkipSim project
# This Makefile provides targets for compiling, testing, and cleaning the project

# Directories
SRC_DIR = src/main/java
TEST_DIR = src/test/java
OUT_DIR = out
PRODUCTION_DIR = $(OUT_DIR)/production
TEST_OUT_DIR = $(OUT_DIR)/test
LIBS_DIR = libs

# Java compiler
JAVAC = javac
JAVA = java

# Classpath
CP_COMPILE = $(LIBS_DIR)/*
CP_TEST = $(LIBS_DIR)/*:$(PRODUCTION_DIR):$(TEST_OUT_DIR)

# Find all Java source files (excluding GUI files that require JavaFX)
SRC_FILES = $(shell find $(SRC_DIR) -name "*.java" ! -name "NewMain.java")
TEST_FILES = $(shell find $(TEST_DIR) -name "*.java")

# Default target
.PHONY: all
all: compile

# Compile main sources
.PHONY: compile
compile:
	@echo "Compiling main sources..."
	@mkdir -p $(PRODUCTION_DIR)
	@$(JAVAC) -d $(PRODUCTION_DIR) -cp "$(CP_COMPILE)" $(SRC_FILES)
	@echo "Main sources compiled successfully."

# Compile test sources
.PHONY: compile-tests
compile-tests: compile
	@echo "Compiling test sources..."
	@mkdir -p $(TEST_OUT_DIR)
	@$(JAVAC) -d $(TEST_OUT_DIR) -cp "$(CP_COMPILE):$(PRODUCTION_DIR)" $(TEST_FILES)
	@echo "Test sources compiled successfully."

# Run tests
.PHONY: test
test: compile-tests
	@echo "Running tests..."
	@$(JAVA) -cp "$(CP_TEST)" org.junit.runner.JUnitCore SkipGraph.TransactionInsertionTest

# Run tests with verbose output
.PHONY: test-verbose
test-verbose: compile-tests
	@echo "Running tests with verbose output..."
	$(JAVA) -cp "$(CP_TEST)" org.junit.runner.JUnitCore SkipGraph.TransactionInsertionTest

# Clean build artifacts
.PHONY: clean
clean:
	@echo "Cleaning build artifacts..."
	@rm -rf $(OUT_DIR)
	@echo "Clean complete."

# Display help
.PHONY: help
help:
	@echo "SkipSim Makefile targets:"
	@echo "  make              - Compile main sources (default)"
	@echo "  make compile      - Compile main sources"
	@echo "  make compile-tests - Compile main and test sources"
	@echo "  make test         - Compile and run tests"
	@echo "  make test-verbose - Run tests with verbose output"
	@echo "  make clean        - Remove build artifacts"
	@echo "  make help         - Show this help message"
