package TestFixtures;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple test runner for executing tests without external dependencies.
 * This provides basic test execution and reporting functionality.
 */
public class TestRunner {

    private static class TestResult {
        String testName;
        boolean passed;
        String message;
        Throwable exception;

        TestResult(String testName, boolean passed, String message, Throwable exception) {
            this.testName = testName;
            this.passed = passed;
            this.message = message;
            this.exception = exception;
        }
    }

    private List<TestResult> results = new ArrayList<>();
    private int testCount = 0;
    private int passedCount = 0;
    private int failedCount = 0;

    /**
     * Runs all test methods in the given test class.
     * Test methods should start with "test" and be public void methods.
     *
     * @param testClass The class containing test methods
     */
    public void runTests(Class<?> testClass) {
        System.out.println("\n=================================");
        System.out.println("Running tests for: " + testClass.getSimpleName());
        System.out.println("=================================\n");

        Object testInstance;
        try {
            testInstance = testClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Failed to instantiate test class: " + e.getMessage());
            return;
        }

        // Find all test methods
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("test") && method.getParameterCount() == 0) {
                runTest(testInstance, method);
            }
        }

        printSummary();
    }

    /**
     * Runs a single test method.
     */
    private void runTest(Object testInstance, Method testMethod) {
        testCount++;
        String testName = testMethod.getName();
        System.out.println("Running: " + testName);

        try {
            testMethod.invoke(testInstance);
            passedCount++;
            results.add(new TestResult(testName, true, "PASSED", null));
            System.out.println("  ✓ PASSED\n");
        } catch (Exception e) {
            failedCount++;
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            results.add(new TestResult(testName, false, "FAILED", cause));
            System.out.println("  ✗ FAILED: " + cause.getMessage());
            if (cause instanceof AssertionError) {
                // Print assertion details
                System.out.println("    " + cause.getMessage());
            } else {
                // Print stack trace for other exceptions
                cause.printStackTrace(System.out);
            }
            System.out.println();
        }
    }

    /**
     * Prints a summary of test results.
     */
    private void printSummary() {
        System.out.println("=================================");
        System.out.println("Test Summary");
        System.out.println("=================================");
        System.out.println("Total tests:  " + testCount);
        System.out.println("Passed:       " + passedCount);
        System.out.println("Failed:       " + failedCount);
        System.out.println("Success rate: " + (testCount > 0 ? (passedCount * 100 / testCount) : 0) + "%");
        System.out.println("=================================\n");

        if (failedCount > 0) {
            System.out.println("Failed tests:");
            for (TestResult result : results) {
                if (!result.passed) {
                    System.out.println("  - " + result.testName + ": " + result.message);
                }
            }
            System.out.println();
        }
    }

    /**
     * Simple assertion utility.
     */
    public static class Assert {
        public static void assertTrue(String message, boolean condition) {
            if (!condition) {
                throw new AssertionError(message);
            }
        }

        public static void assertFalse(String message, boolean condition) {
            if (condition) {
                throw new AssertionError(message);
            }
        }

        public static void assertEquals(String message, Object expected, Object actual) {
            if (expected == null && actual == null) {
                return;
            }
            if (expected == null || !expected.equals(actual)) {
                throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
            }
        }

        public static void assertNotNull(String message, Object object) {
            if (object == null) {
                throw new AssertionError(message);
            }
        }

        public static void assertNull(String message, Object object) {
            if (object != null) {
                throw new AssertionError(message + " - Expected null but got: " + object);
            }
        }

        public static void fail(String message) {
            throw new AssertionError(message);
        }
    }

    /**
     * Returns whether all tests passed.
     */
    public boolean allTestsPassed() {
        return failedCount == 0;
    }
}
