import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


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
 * @version 0.1
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
        int priority() default 11;
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
        Method method;
        T annotation;

        public Entry(Method method, T annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_ERROR = "\u001B[31m";
    private static final String ANSI_GOOD = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private static final String MSG_GOOD = "ok";

    /**
     * Internal error runtime exception
     */
    static class JTestAssertException extends RuntimeException {
        public JTestAssertException(String message) {
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
        public static void Equals(int expected, int got) {
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
        public static void Equals(long expected, long got) {
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
        public static void Equals(Comparable expected, Comparable got) {
            if (got.compareTo(expected) != 0)
                throw new JTestAssertException("Values(Comparable) are not equal!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=" + expected + "\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }

        /**
         * Checks, if values is null (Object)
         *
         * @param got value, expected equals to {@code null}
         * @throws JTestAssertException when {@code got} is not {@code null}
         */
        public static void IsNull(Object got) {
            if (got == null)
                throw new JTestAssertException("Value(Object) are not null!\n"
                        + ANSI_GOOD + "    expected" + ANSI_RESET + "=null\n"
                        + ANSI_ERROR + "         got" + ANSI_RESET + "=" + got);
        }
    }

    /**
     * Internal clazz storage
     */
    private Class clazz;

    /**
     * Table of all Entries in {@code clazz}
     * HashMap:
     *   key - Annotation type
     *   value - List of Entries
     * @see Entry
     */
    private Map<Class<?>, List<Entry<Annotation>>> entries;

    /**
     * Comparator to sort @Test {@code Entry} list by priority
     */
    private static Comparator<Entry<Annotation>> AnnotationTestPrioritySort = (e1, e2) -> {
        Test test1 = (Test) e1.annotation;
        Test test2 = (Test) e2.annotation;
        return test1.priority() - test2.priority();
    };

    /**
     * Run tests for class, named by {@code name}
     * Generates console messages on tests execution
     * Creates class {@code clazz} and {@code entries}
     *
     * @param name full name of test class
     * @throws RuntimeException       raises when class have several {@code BeforeEach} or {@code AfterEach} methods
     * @throws ClassNotFoundException when class name {@code name} not found
     */
    void run(String name) throws Exception {

        clazz = Class.forName(name);
        System.out.println(clazz.getSimpleName() + ':');

        entries = new Hashtable<Class<?>, List<Entry<Annotation>>>();

        readMethodsAnnotations();
        checkRules();
        runTests();
    }

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

        for (Entry<Annotation> test : testList) {
            try {
                System.out.print(test.method.getName() + " ... ");
                runTest(test, before, after);
                System.out.println(MSG_GOOD);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException | JTestAssertException er) {
                Throwable t = er.getCause();
                if (t.getClass() == JTestAssertException.class) // check if self-generated Exception
                    System.out.println(t.getMessage()); // only short message
                else er.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    private void runTest(Entry<Annotation> test, Entry<Annotation> before, Entry<Annotation> after) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        if (before != null)
            before.method.invoke(instance);
        test.method.invoke(instance);
        if (after != null)
            after.method.invoke(instance);
    }

    /**
     * Read all annotated methods to arrays: befores, tests, afters
     */
    private void readMethodsAnnotations() {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation a : annotations) {
                Class<? extends Annotation> aType = a.annotationType();
                List<Entry<Annotation>> list = entries.get(aType);
                if (list == null) {
                    list = new ArrayList();
                    entries.put(aType, list);
                }
                list.add(new Entry<>(method, a));
            }
        }
    }

    /**
     * Check annotation rules
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

}