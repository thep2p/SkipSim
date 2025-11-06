# JUnit Setup for SkipSim

## Adding JUnit to the Project

### Option 1: Using IntelliJ IDEA (Recommended)

1. **Download JUnit JARs:**
   - Download `junit-4.13.2.jar` from [Maven Repository](https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar)
   - Download `hamcrest-core-1.3.jar` from [Maven Repository](https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar)

2. **Add to libs directory:**
   ```bash
   # Save the downloaded JARs to the libs directory
   cp junit-4.13.2.jar /path/to/SkipSim/libs/
   cp hamcrest-core-1.3.jar /path/to/SkipSim/libs/
   ```

3. **Add to IntelliJ Project:**
   - Open SkipSim in IntelliJ IDEA
   - File → Project Structure (⌘;)
   - Select "Libraries" on the left
   - Click "+" → "Java"
   - Navigate to `libs/` directory
   - Select both `junit-4.13.2.jar` and `hamcrest-core-1.3.jar`
   - Click OK

4. **Mark test directory as Test Sources:**
   - Right-click on `src/test/java` in Project view
   - Mark Directory as → Test Sources Root

### Option 2: Quick IntelliJ Setup

IntelliJ can automatically download JUnit for you:

1. Open any test file (e.g., `TransactionInsertionTest.java`)
2. Place cursor on the red `@Test` annotation
3. Press Alt+Enter (⌥↵ on Mac)
4. Select "Add JUnit 4 to classpath"
5. Click OK to download and add JUnit

## Running Tests

### From IntelliJ

**Run a single test:**
- Click the green arrow next to the test method
- Or right-click on the test method → Run 'testMethodName()'

**Run all tests in a class:**
- Click the green arrow next to the class name
- Or right-click on the class → Run 'TransactionInsertionTest'

**Run all tests in the project:**
- Right-click on `src/test/java` directory
- Select "Run 'All Tests'"

### From Command Line

```bash
# Compile tests
javac -cp "libs/*:src/main/java:src/test/java" \
  -d out \
  src/test/java/SkipGraph/TransactionInsertionTest.java

# Run tests
java -cp "libs/*:out" \
  org.junit.runner.JUnitCore SkipGraph.TransactionInsertionTest
```

## Viewing Test Results

IntelliJ provides a test runner panel showing:
- ✓ Passed tests (green)
- ✗ Failed tests (red)
- Test execution time
- Failure messages and stack traces
- Ability to re-run failed tests

## Continuous Integration

For CI/CD pipelines, you can run tests with:

```bash
# Using JUnit Console Launcher (recommended for CI)
java -jar junit-platform-console-standalone.jar \
  --class-path out \
  --scan-class-path
```

## Why JUnit?

JUnit is the industry-standard testing framework for Java, providing:

1. **Better IDE Integration:**
   - IntelliJ provides excellent JUnit support
   - Run/debug tests with one click
   - Visual test runner with progress indicators
   - Quick navigation to failed assertions

2. **Rich Assertion Library:**
   - More readable: `assertEquals(expected, actual)`
   - Better failure messages
   - Hamcrest matchers for complex assertions

3. **Test Lifecycle Management:**
   - `@Before` / `@After` for setup/teardown
   - `@BeforeClass` / `@AfterClass` for one-time setup
   - `@Test(expected=Exception.class)` for exception testing
   - `@Test(timeout=1000)` for performance tests

4. **Community & Ecosystem:**
   - Industry standard (used by 70%+ of Java projects)
   - Extensive documentation and examples
   - Integrates with all major CI/CD systems
   - Support for parameterized tests, test suites, etc.

5. **Better Test Reports:**
   - JUnit generates standard XML reports
   - Easy to integrate with reporting tools
   - Better visibility in CI/CD pipelines

## Example Test Structure

```java
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyTest {
    private MyClass instance;

    @Before
    public void setUp() {
        // Runs before each test
        instance = new MyClass();
    }

    @After
    public void tearDown() {
        // Runs after each test
        instance = null;
    }

    @Test
    public void testSomething() {
        int result = instance.doSomething();
        assertEquals("Should return 42", 42, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionThrown() {
        instance.methodThatThrows();
    }

    @Test(timeout = 1000)
    public void testPerformance() {
        // Should complete within 1 second
        instance.fastOperation();
    }
}
```

## Troubleshooting

**Problem:** IntelliJ shows "Cannot resolve symbol 'Test'"

**Solution:**
- Ensure JUnit is added to project libraries
- File → Project Structure → Libraries → Check if JUnit is listed
- Invalidate Caches: File → Invalidate Caches / Restart

**Problem:** Tests don't run from command line

**Solution:**
- Ensure JUnit JAR is in classpath
- Check that compiled test classes are in output directory
- Verify package structure matches directory structure

**Problem:** "No tests found"

**Solution:**
- Check that test methods are public
- Ensure methods have `@Test` annotation
- Verify class is public and has default constructor
- Check that test source directory is marked correctly
