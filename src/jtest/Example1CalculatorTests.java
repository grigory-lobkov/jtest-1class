package jtest;

import static jtest.JTest.*;
import static jtest.JTest.Assert.*;

public class Example1CalculatorTests {

    private Example1Calculator calculator;

    @BeforeEach
    public void init() {
        calculator = new Example1Calculator();
    }

    @Test(priority = 1)
    public void test_sum() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 + v2;
        int got = calculator.sum(v1, v2);
        assertEquals(expected, got);
    }

    @Test
    public void test_diff() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 - v2;
        int got = calculator.diff(v1, v2);
        assertEquals(expected, got);
    }

    @Test(priority = 2)
    public void test_mult() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 * v2 + 10000; // Expected is wrong to get error example
        int got = calculator.mult(v1, v2);
        assertEquals(expected, got);
    }

    @Test(priority = 3)
    public void test_div() {
        int v1 = 30;
        int v2 = 5;
        int expected = v1 / v2;
        int got = calculator.div(v1, v2);
        assertEquals(expected, got);
    }

    @AfterEach
    public void close() {
        calculator = null;
    }

}
