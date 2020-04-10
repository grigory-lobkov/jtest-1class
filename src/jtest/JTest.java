package jtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


/**
 * Java classes simple test framework
 * Supported annotations: {@code @BeforeEach}, {@code @Test}, {@code @AfterEach}
 * @see BeforeEach
 * @see Test
 * @see AfterEach
 *
 * Supported Assertions
 * @see Assert
 *
 * @author Gregory Lobkov
 * @link <a href="https://github.com/grigory-lobkov/jtest-1class">Project page</a>
 * @version 0.1
 * @since 1.5
 */

public class JTest {


    /**
     * Executes before each {@code Test}.
     * Must be alone in the class.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BeforeEach {
    }


    /**
     * Executes as a test method.
     * Methods must be public.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Test {
        /**
         * Priority. Valid values is from 1 to 10 (including).
         * All other methods will be executed after prioritized ones.
         */
        int priority() default Integer.MAX_VALUE;
    }


    /**
     * Executes after each {@code Test}.
     * Must be alone in the class.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AfterEach {
    }


    /**
     * Entry for storing {@code Method} and {@code Annotation} pair in lists
     *
     * @param <T> custom type of {@code Annotation}
     */
    class Entry<T extends Annotation> {
        final Method method;
        final T annotation;

        Entry(Method method, T annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }


    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_ERROR = "\u001B[31m";
    private static final String ANSI_GOOD = "\u001B[32m";

    private static final String MSG_GOOD = "ok";


    /**
     * Internal error runtime exception
     */
    static class JTestAssertException extends RuntimeException {
        JTestAssertException(String message) {
            super(message);
        }
    }


    /**
     * Help class for comparing values
     */
    public static class Assert {
        /**
         * Checks, if values are equals (int, int)
         *
         * @param expected value, expected as a result
         * @param got      value, got from method
         * @throws JTestAssertException when {@code got} is not equals to {@code expected}
         */
        public static void assertEquals(int expected, int got) {
            if (got != expected)
                throw new JTestAssertException("Values(int) are not equal!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=" + expected + "\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

        /**
         * Checks, if values are equals (long, long)
         *
         * @param expected value, expected as a result
         * @param got      value, got from method
         * @throws JTestAssertException when {@code got} is not equals to {@code expected}
         */
        public static void assertEquals(long expected, long got) {
            if (got != expected)
                throw new JTestAssertException("Values(long) are not equal!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=" + expected + "\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

        /**
         * Checks, if values are equals (Comparable, Comparable)
         *
         * @param expected value, expected as a result
         * @param got      value, got from method
         * @throws JTestAssertException when {@code got} is not equals to {@code expected}
         */
        public static void assertEquals(Comparable expected, Comparable got) {
            //noinspection unchecked
            if (got.compareTo(expected) != 0)
                throw new JTestAssertException("Values("+got.getClass().getSimpleName()+") are not equal!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=" + expected + "\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

        /**
         * Checks, if value is null (Object)
         *
         * @param got value, expected equals to {@code null}
         * @throws JTestAssertException when {@code got} is not {@code null}
         */
        public static void assertNull(Object got) {
            if (got != null)
                throw new JTestAssertException("Value("+got.getClass().getSimpleName()+") are not null!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=null\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

        /**
         * Checks, if boolean value is true
         *
         * @param got value, expected equals to {@code true}
         * @throws JTestAssertException when {@code got} is not {@code true}
         */
        public static void assertTrue(boolean got) {
            if (!got)
                throw new JTestAssertException("Value(boolean) are not true!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=true\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

        /**
         * Checks, if boolean value is false
         *
         * @param got value, expected equals to {@code false}
         * @throws JTestAssertException when {@code got} is not {@code false}
         */
        public static void assertFalse(boolean got) {
            if (got)
                throw new JTestAssertException("Value(boolean) are not false!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=false\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

    }


    /**
     * Internal clazz storage
     */
    final private Class clazz;


    /**
     * Table of all Entries in {@code clazz}
     * HashMap:
     * key - Annotation type
     * value - List of Entries
     *
     * @see Entry
     */
    final private Map<Class<?>, List<Entry<Annotation>>> entries;


    /**
     * Current testing instance
     */
    private Object instance;


    /**
     * Comparator to sort @Test {@code Entry} list by priority
     */
    final private static Comparator<Entry<Annotation>> AnnotationTestPrioritySort = new Comparator<Entry<Annotation>>() {
        public int compare(Entry<Annotation> e1, Entry<Annotation> e2) {

            Test test1 = (Test) e1.annotation;
            Test test2 = (Test) e2.annotation;

            return test1.priority() - test2.priority();

        }
    };


    /**
     * Run all {@code Test} methods
     * Sorts @Test List by priority
     * Uses class {@code clazz} and {@code entries}
     *
     * @see JTest#clazz
     */
    private void runTests() {

        List<Entry<Annotation>> beforeList = entries.get(BeforeEach.class);
        List<Entry<Annotation>> testList = entries.get(Test.class);
        List<Entry<Annotation>> afterList = entries.get(AfterEach.class);

        Entry<Annotation> before = beforeList == null ? null : beforeList.get(0);
        Entry<Annotation> after = beforeList == null ? null : afterList.get(0);

        Collections.sort(testList, AnnotationTestPrioritySort);

        try {
            //noinspection unchecked
            instance = clazz.getDeclaredConstructor().newInstance();

            for (Entry<Annotation> test : testList) {
                try {
                    System.out.print(test.method.getName() + " ... ");

                    runTest(test, before, after);

                    System.out.println(MSG_GOOD);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    Throwable t = e.getCause();
                    if (t.getClass() == JTestAssertException.class) // check if self-generated Exception
                        System.out.println(t.getMessage()); // only short message
                    else e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Run one {@code Test} method
     * Creates an instance of a {@code clazz} and runs linked methods
     *
     * @param test   Entry {@code Test} for test
     * @param before Entry {@code BeforeEach} for test
     * @param after  Entry {@code AfterEach} for test
     */
    private void runTest(Entry<Annotation> test, Entry<Annotation> before, Entry<Annotation> after) throws InvocationTargetException, IllegalAccessException {

        if (before != null)
            before.method.invoke(instance);

        test.method.invoke(instance);

        if (after != null)
            after.method.invoke(instance);

    }


    /**
     * Check annotation rules, stored in {@code entries}
     */
    private void checkRules() {
        List<Entry<Annotation>> beforeList = entries.get(BeforeEach.class);
        List<Entry<Annotation>> testList = entries.get(Test.class);
        List<Entry<Annotation>> afterList = entries.get(AfterEach.class);

        if (beforeList != null && beforeList.size() > 1)
            throw new RuntimeException("@BeforeEach method must be alone");
        if (afterList != null && afterList.size() > 1)
            throw new RuntimeException("@AfterEach method must be alone");
        if (testList == null || testList.size() == 0)
            throw new RuntimeException("@Test methods not found");
    }


    /**
     * Read all annotated methods in {@code clazz} to {@code entries}
     * @see JTest#entries
     */
    private void readMethodsAnnotations() {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation a : annotations) {
                Class<? extends Annotation> aType = a.annotationType();
                List<Entry<Annotation>> list = entries.get(aType);
                if (list == null) {
                    list = new ArrayList<Entry<Annotation>>();
                    entries.put(aType, list);
                }
                list.add(new Entry<Annotation>(method, a));
            }
        }
    }


    /**
     * Parses annotations for class {@code clazz}
     * Creates class {@code clazz} and {@code entries}
     *
     * @param clazz test class
     * @throws RuntimeException raises when class have several {@code BeforeEach} or {@code AfterEach} methods
     */
    public JTest(Class clazz) {

        this.clazz = clazz;
        entries = new Hashtable<Class<?>, List<Entry<Annotation>>>();

        readMethodsAnnotations();
        checkRules();

    }


    /**
     * Run tests for {@code clazz}
     * Generates console messages on tests execution
     * @see JTest#clazz
     */
    public void run() {

        System.out.println(clazz.getName() + ':');

        runTests();

    }

}