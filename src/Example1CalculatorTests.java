public class Example1CalculatorTests {

    Example1Calculator calculator;

    @JTest.BeforeEach
    public void init() {
        calculator = new Example1Calculator();
    }

    @JTest.Test(priority = 1)
    public void test_sum() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 + v2;
        int got = calculator.sum(v1, v2);
        JTest.Assert.Equals(expected, got);
    }

    @JTest.Test
    public void test_diff() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 - v2;
        int got = calculator.diff(v1, v2);
        JTest.Assert.Equals(expected, got);
    }

    @JTest.Test(priority = 2)
    public void test_mult() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 * v2 + 10000; // Expected is wrong to get error example
        int got = calculator.mult(v1, v2);
        JTest.Assert.Equals(expected, got);
    }

    @JTest.Test(priority = 3)
    public void test_div() {
        int v1 = 30;
        int v2 = 5;
        int expected = v1 / v2;
        int got = calculator.div(v1, v2);
        JTest.Assert.Equals(expected, got);
    }

    @JTest.AfterEach
    public void close() {
        calculator = null;
    }

}
