# Contributing to SkipSim

Thank you for your interest in contributing to SkipSim! This document provides guidelines for contributing to the project.

## Getting Started

### Prerequisites

- Java 11 or higher (Java 11, 17, and 21 are tested in CI)
- Maven 3.6 or higher
- Git

### Setting Up Development Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/thep2p/SkipSim.git
   cd SkipSim
   ```

2. Download dependencies:
   ```bash
   make install
   # or
   mvn dependency:resolve
   ```

3. Compile the project:
   ```bash
   make compile
   # or
   mvn compile
   ```

4. Run tests:
   ```bash
   make test
   # or
   mvn test
   ```

## Development Workflow

### Building and Testing

The project uses Maven as its build system. Common commands:

```bash
# Compile sources
make compile  # or mvn compile

# Run all tests
make test  # or mvn test

# Run tests with verbose output
make test-verbose  # or mvn test -X

# Clean build artifacts
make clean  # or mvn clean

# Full build and test cycle
mvn clean compile test
```

### Writing Tests

- Tests are located in `src/test/java/`
- We use JUnit 4 for testing
- Use the `SkipGraphTestFixture` class for creating test scenarios
- Always add tests for new features and bug fixes

Example test:
```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyFeatureTest {
    private SkipGraphTestFixture fixture;

    @Before
    public void setUp() {
        fixture = new SkipGraphTestFixture();
    }

    @Test
    public void testMyFeature() {
        // Arrange
        List<Node> nodes = fixture.createAndInsertNodes(10);

        // Act
        // ... test your feature

        // Assert
        assertNotNull("Result should not be null", result);
    }
}
```

## Continuous Integration

### CI Workflow

The project uses GitHub Actions for continuous integration. The CI workflow:

1. Tests on multiple Java versions (11, 17, 21)
2. Compiles sources with Maven
3. Runs all unit tests
4. Uploads test results as artifacts

### Validating Changes Locally

Before pushing changes, validate them locally:

```bash
# Quick validation
make clean && make test

# Full CI simulation
.github/scripts/validate-ci.sh

# Or manually
mvn clean
mvn dependency:resolve
mvn compile
mvn test
```

### CI Status

[![CI](https://github.com/thep2p/SkipSim/actions/workflows/ci.yml/badge.svg)](https://github.com/thep2p/SkipSim/actions/workflows/ci.yml)

The CI badge shows the current build status. All pull requests must pass CI checks before merging.

## Pull Request Process

1. **Fork the repository** and create a feature branch from `master`

2. **Make your changes** following the coding standards:
   - Use meaningful variable and method names
   - Add JavaDoc comments for public methods
   - Follow existing code style
   - Keep methods focused and concise

3. **Write tests** for your changes:
   - Add unit tests for new functionality
   - Add regression tests for bug fixes
   - Ensure all tests pass locally

4. **Validate your changes**:
   ```bash
   .github/scripts/validate-ci.sh
   ```

5. **Commit your changes** with clear commit messages:
   ```bash
   git commit -m "Add feature X to improve Y"
   ```

6. **Push to your fork** and create a pull request:
   - Provide a clear description of the changes
   - Reference any related issues
   - Ensure CI passes

7. **Respond to review feedback** and make necessary changes

## Code Style Guidelines

### Java Code Style

- Use 4 spaces for indentation (no tabs)
- Opening braces on the same line
- Use meaningful variable names
- Add comments for complex logic
- Maximum line length: 120 characters

### Test Naming

- Test class names should end with `Test`
- Test method names should be descriptive: `testFeatureName_Scenario_ExpectedResult`
- Use setup methods (`@Before`) for common initialization

## Reporting Issues

When reporting issues, please include:

1. **Description** - Clear description of the issue
2. **Steps to reproduce** - Minimal steps to reproduce the problem
3. **Expected behavior** - What you expected to happen
4. **Actual behavior** - What actually happened
5. **Environment** - Java version, OS, Maven version
6. **Logs/screenshots** - Any relevant error messages or output

## Getting Help

- Check the [README.md](../README.md) for documentation
- Review existing issues and pull requests
- Check [CLAUDE.md](../CLAUDE.md) for project structure details

## License

By contributing, you agree that your contributions will be licensed under the project's license.

## Questions?

Feel free to open an issue for any questions about contributing!
