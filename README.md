# jTest

## Java classes simple test framework

JUnit-like class to test your programs for Java. It will help you in creating tests for your classes. The main goal of my "1class" classes is do not to use any dependencies.

## Usage

- Copy "src/JTest.java" to your project

- Create instance of tester, ex: `JTest test = new JTest();`

- Process some test class, ex: `test.run("Example1CalculatorTests");`

## Annotations for methods

`@Test` - execute method of test class as a test

`@BeforeEach` - execute part of code in the beginning of each `@Test`

`@AfterEach` - execute after each `@Test`

## @Test properties

int `priority` - to set priority for test and make some order in execution

## Assertions

`Assert.equals` - check if equals for `int`, `long`, `Comparable`

`Assert.isNull` - check if `Object` is `null`

## Contribution

I wanted to create simple JUnit 5.x compatible framework, easy to use and deploy, without dependencies. You are welcome to improve it.
