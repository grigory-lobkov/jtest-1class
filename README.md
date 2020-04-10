# jTest

## Java classes simple test framework

JUnit-like class to test your programs for Java. It will help you in creating tests for your classes. The main goal of my "1class" classes is do not to use any dependencies.

## Usage

- Copy "src/jtest/JTest.java" to your project

- Create instance of tester for test class and process tests, ex: `new JTest(Example1CalculatorTests.class).run();`

## Annotations for methods

`@Test` - execute method of test class as a test

`@BeforeEach` - execute part of code in the beginning of each `@Test`

`@AfterEach` - execute after each `@Test`

## @Test properties

int `priority` - to set priority for test and make some order in execution

## Assertions

`Assert.assertEquals` - check if equals for `int`, `long`, `Comparable`

`Assert.assertNull` - check if `Object` is `null`

`Assert.assertTrue` - check if `boolean` is `true`

`Assert.assertFalse` - check if `boolean` is `false`

## Contribution

We are creating simple [JUnit 5.x](https://junit.org/junit5/docs/current/user-guide/) compatible framework, easy to use and deploy, without dependencies. You are welcome to improve it. Target Java version is 1.5 for now.
