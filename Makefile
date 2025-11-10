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
CP_RUN = $(LIBS_DIR)/*:$(PRODUCTION_DIR)

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

# Run simulation commands
.PHONY: run-new
run-new: compile
	@if [ -z "$(NAME)" ]; then \
		echo "Error: Simulation name required"; \
		echo "Usage: make run-new NAME=<simulation-name> [CONFIG=<config-file>]"; \
		exit 1; \
	fi
	@echo "Creating new simulation: $(NAME)"
	@if [ -n "$(CONFIG)" ]; then \
		echo "Using config file: $(CONFIG)"; \
		$(JAVA) -cp "$(CP_RUN)" Simulator.Main new $(NAME) --config $(CONFIG); \
	else \
		$(JAVA) -cp "$(CP_RUN)" Simulator.Main new $(NAME); \
	fi

.PHONY: run-load
run-load: compile
	@if [ -z "$(NAME)" ]; then \
		echo "Error: Simulation name required"; \
		echo "Usage: make run-load NAME=<simulation-name> [CONFIG=<config-file>]"; \
		exit 1; \
	fi
	@echo "Loading simulation: $(NAME)"
	@if [ -n "$(CONFIG)" ]; then \
		echo "Using config file: $(CONFIG)"; \
		$(JAVA) -cp "$(CP_RUN)" Simulator.Main load $(NAME) --config $(CONFIG); \
	else \
		$(JAVA) -cp "$(CP_RUN)" Simulator.Main load $(NAME); \
	fi

.PHONY: run-list
run-list: compile
	@echo "Listing available simulations..."
	@if [ -n "$(CONFIG)" ]; then \
		echo "Using config file: $(CONFIG)"; \
		$(JAVA) -cp "$(CP_RUN)" Simulator.Main list --config $(CONFIG); \
	else \
		$(JAVA) -cp "$(CP_RUN)" Simulator.Main list; \
	fi

.PHONY: run-delete
run-delete: compile
	@if [ -z "$(NAME)" ]; then \
		echo "Error: Simulation name required"; \
		echo "Usage: make run-delete NAME=<simulation-name>"; \
		exit 1; \
	fi
	@echo "Deleting simulation: $(NAME)"
	@$(JAVA) -cp "$(CP_RUN)" Simulator.Main delete $(NAME)

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
	@echo ""
	@echo "Build targets:"
	@echo "  make              - Compile main sources (default)"
	@echo "  make compile      - Compile main sources"
	@echo "  make compile-tests - Compile main and test sources"
	@echo "  make clean        - Remove build artifacts"
	@echo ""
	@echo "Test targets:"
	@echo "  make test         - Compile and run tests"
	@echo "  make test-verbose - Run tests with verbose output"
	@echo ""
	@echo "Simulation targets:"
	@echo "  make run-load NAME=<name> [CONFIG=<file>]   - Load or create simulation"
	@echo "  make run-new NAME=<name> [CONFIG=<file>]    - Create new simulation"
	@echo "  make run-list                               - List available simulations"
	@echo "  make run-delete NAME=<name>                 - Delete a simulation"
	@echo ""
	@echo "Examples:"
	@echo "  make run-load NAME=my_sim"
	@echo "  make run-load NAME=my_sim CONFIG=configs/quick-test.properties"
	@echo "  make run-new NAME=my_sim CONFIG=configs/full-experiment.properties"
	@echo "  make run-list"
	@echo ""
	@echo "Configuration:"
	@echo "  Default config: simulation-config.properties"
	@echo "  Custom configs: configs/quick-test.properties, configs/full-experiment.properties"
	@echo "  Edit config files to change parameters without recompilation!"
	@echo ""
	@echo "Help:"
	@echo "  make help         - Show this help message"
