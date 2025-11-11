# Makefile for SkipSim project
# This Makefile provides convenient wrappers around Maven commands

# Maven executable
MVN = mvn

# Directories (for backwards compatibility and direct java runs)
OUT_DIR = out
PRODUCTION_DIR = $(OUT_DIR)/production
LIBS_DIR = libs

# Default target
.PHONY: all
all: compile

# Download dependencies (first time setup)
.PHONY: install
install:
	@echo "Downloading Maven dependencies..."
	@$(MVN) dependency:resolve
	@echo "Dependencies downloaded successfully."

# Compile main sources
.PHONY: compile
compile:
	@echo "Compiling main sources with Maven..."
	@$(MVN) compile

# Compile test sources
.PHONY: compile-tests
compile-tests:
	@echo "Compiling test sources with Maven..."
	@$(MVN) test-compile

# Run tests
.PHONY: test
test:
	@echo "Running tests with Maven..."
	@$(MVN) test

# Run tests with verbose output
.PHONY: test-verbose
test-verbose:
	@echo "Running tests with verbose output..."
	@$(MVN) test -X

# Run simulation commands using Maven exec plugin
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
		$(MVN) exec:java -Dexec.args="new $(NAME) --config $(CONFIG)"; \
	else \
		$(MVN) exec:java -Dexec.args="new $(NAME)"; \
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
		$(MVN) exec:java -Dexec.args="load $(NAME) --config $(CONFIG)"; \
	else \
		$(MVN) exec:java -Dexec.args="load $(NAME)"; \
	fi

.PHONY: run-list
run-list: compile
	@echo "Listing available simulations..."
	@if [ -n "$(CONFIG)" ]; then \
		echo "Using config file: $(CONFIG)"; \
		$(MVN) exec:java -Dexec.args="list --config $(CONFIG)"; \
	else \
		$(MVN) exec:java -Dexec.args="list"; \
	fi

.PHONY: run-delete
run-delete: compile
	@if [ -z "$(NAME)" ]; then \
		echo "Error: Simulation name required"; \
		echo "Usage: make run-delete NAME=<simulation-name>"; \
		exit 1; \
	fi
	@echo "Deleting simulation: $(NAME)"
	@$(MVN) exec:java -Dexec.args="delete $(NAME)"

# Clean build artifacts
.PHONY: clean
clean:
	@echo "Cleaning build artifacts with Maven..."
	@$(MVN) clean
	@echo "Clean complete."

# Display help
.PHONY: help
help:
	@echo "SkipSim Makefile - Maven-based build system"
	@echo ""
	@echo "Setup:"
	@echo "  make install      - Download Maven dependencies (first time setup)"
	@echo ""
	@echo "Build targets:"
	@echo "  make              - Compile main sources (default)"
	@echo "  make compile      - Compile main sources"
	@echo "  make compile-tests - Compile main and test sources"
	@echo "  make clean        - Remove build artifacts"
	@echo ""
	@echo "Test targets:"
	@echo "  make test         - Compile and run all tests"
	@echo "  make test-verbose - Run tests with verbose Maven output"
	@echo ""
	@echo "Simulation targets:"
	@echo "  make run-load NAME=<name> [CONFIG=<file>]   - Load or create simulation"
	@echo "  make run-new NAME=<name> [CONFIG=<file>]    - Create new simulation"
	@echo "  make run-list                               - List available simulations"
	@echo "  make run-delete NAME=<name>                 - Delete a simulation"
	@echo ""
	@echo "Examples:"
	@echo "  make install                              # First time setup"
	@echo "  make compile                              # Build the project"
	@echo "  make test                                 # Run tests"
	@echo "  make run-load NAME=my_sim"
	@echo "  make run-load NAME=my_sim CONFIG=configs/quick-test.properties"
	@echo "  make run-new NAME=my_sim CONFIG=configs/full-experiment.properties"
	@echo "  make run-list"
	@echo ""
	@echo "Maven commands (alternative):"
	@echo "  mvn compile                               # Compile sources"
	@echo "  mvn test                                  # Run tests"
	@echo "  mvn exec:java -Dexec.args=\"list\"          # Run simulator"
	@echo ""
	@echo "Help:"
	@echo "  make help         - Show this help message"
