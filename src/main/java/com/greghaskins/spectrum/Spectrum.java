package com.greghaskins.spectrum;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class Spectrum extends Runner {

  /**
   * A generic code block with a {@link #run()} method to perform any action. Usually defined by a
   * lambda function.
   * @deprecated since 1.0.1 - use {@link com.greghaskins.spectrum.Block} instead
   */
  @Deprecated
  @FunctionalInterface
  public interface Block extends com.greghaskins.spectrum.Block {
    /**
     * Execute the code block, raising any {@code Throwable} that may occur.
     *
     * @throws Throwable any uncaught Error or Exception
     */
    @Override
    void run() throws Throwable;
  }

  /**
   * Declare a test suite that describes the expected behavior of the system in a given context.
   *
   * @param context Description of the context for this suite
   * @param block {@link Block} with one or more calls to {@link #it(String, Block) it} that define
   *        each expected behavior
   *
   */
  public static void describe(final String context, final com.greghaskins.spectrum.Block block) {
    final Suite suite = getCurrentSuiteBeingDeclared().addSuite(context);
    beginDefinition(suite, block);
  }

  /**
   * Focus on this specific suite, while ignoring others.
   *
   * @param context Description of the context for this suite
   * @param block {@link Block} with one or more calls to {@link #it(String, Block) it} that define
   *        each expected behavior
   *
   * @see #describe(String, Block)
   *
   */
  public static void fdescribe(final String context, final com.greghaskins.spectrum.Block block) {
    final Suite suite = getCurrentSuiteBeingDeclared().addSuite(context);
    suite.focus();
    beginDefinition(suite, block);
  }

  /**
   * Ignore the specific suite.
   *
   * @param context Description of the context for this suite
   * @param block {@link Block} with one or more calls to {@link #it(String, Block) it} that define
   *        each expected behavior
   *
   * @see #describe(String, Block)
   *
   */
  public static void xdescribe(final String context, final com.greghaskins.spectrum.Block block) {
    final Suite suite = getCurrentSuiteBeingDeclared().addSuite(context);
    suite.ignore();
    beginDefinition(suite, block);
  }

  /**
   * Declare a spec, or test, for an expected behavior of the system in this suite context.
   *
   * @param behavior Description of the expected behavior
   * @param block {@link Block} that verifies the system behaves as expected and throws a
   *        {@link java.lang.Throwable Throwable} if that expectation is not met.
   */
  public static void it(final String behavior, final com.greghaskins.spectrum.Block block) {
    getCurrentSuiteBeingDeclared().addSpec(behavior, block);
  }

  /**
   * Declare a pending spec (without a block) that will be ignored.
   *
   * @param behavior Description of the expected behavior
   *
   * @see #xit(String, Block)
   */
  public static void it(final String behavior) {
    getCurrentSuiteBeingDeclared().addSpec(behavior, null).ignore();
  }

  /**
   * Focus on this specific spec, while ignoring others.
   *
   * @param behavior Description of the expected behavior
   * @param block {@link Block} that verifies the system behaves as expected and throws a
   *        {@link java.lang.Throwable Throwable} if that expectation is not met.
   *
   * @see #it(String, Block)
   */
  public static void fit(final String behavior, final com.greghaskins.spectrum.Block block) {
    getCurrentSuiteBeingDeclared().addSpec(behavior, block).focus();
  }

  /**
   * Mark a spec as ignored so that it will be skipped.
   *
   * @param behavior Description of the expected behavior
   * @param block {@link Block} that will not run, since this spec is ignored.
   *
   * @see #it(String, Block)
   */
  public static void xit(final String behavior, final com.greghaskins.spectrum.Block block) {
    it(behavior);
  }

  /**
   * Declare a {@link Block} to be run before each spec in the suite.
   *
   * <p>
   * Use this to perform setup actions that are common across tests in the context. If multiple
   * {@code beforeEach} blocks are declared, they will run in declaration order.
   * </p>
   *
   * @param block {@link Block} to run once before each spec
   */
  public static void beforeEach(final com.greghaskins.spectrum.Block block) {
    getCurrentSuiteBeingDeclared().beforeEach(block);
  }

  /**
   * Declare a {@link Block} to be run after each spec in the current suite.
   *
   * <p>
   * Use this to perform teardown or cleanup actions that are common across specs in this suite. If
   * multiple {@code afterEach} blocks are declared, they will run in declaration order.
   * </p>
   *
   * @param block {@link Block} to run once after each spec
   */
  public static void afterEach(final com.greghaskins.spectrum.Block block) {
    getCurrentSuiteBeingDeclared().afterEach(block);
  }

  /**
   * Declare a {@link Block} to be run once before all the specs in the current suite begin.
   *
   * <p>
   * Use {@code beforeAll} and {@link #afterAll(Block) afterAll} blocks with caution: since they
   * only run once, shared state <strong>will</strong> leak across specs.
   * </p>
   *
   * @param block {@link Block} to run once before all specs in this suite
   */
  public static void beforeAll(final com.greghaskins.spectrum.Block block) {
    getCurrentSuiteBeingDeclared().beforeAll(block);
  }

  /**
   * Declare a {@link Block} to be run once after all the specs in the current suite have run.
   *
   * <p>
   * Use {@link #beforeAll(Block) beforeAll} and {@code afterAll} blocks with caution: since they
   * only run once, shared state <strong>will</strong> leak across tests.
   * </p>
   *
   * @param block {@link Block} to run once after all specs in this suite
   */
  public static void afterAll(final com.greghaskins.spectrum.Block block) {
    getCurrentSuiteBeingDeclared().afterAll(block);
  }

  /**
   * Define a memoized helper function. The value will be cached across multiple calls in the same
   * spec, but not across specs.
   *
   * <p>
   * Note that {@code let} is lazy-evaluated: the {@code supplier} is not called until the first
   * time it is used.
   * </p>
   *
   * @param <T> The type of value
   *
   * @param supplier {@link ThrowingSupplier} function that either generates the value, or throws a
   *        `Throwable`
   * @return memoized supplier
   */
  public static <T> Supplier<T> let(final ThrowingSupplier<T> supplier) {
    final ConcurrentHashMap<Supplier<T>, T> cache = new ConcurrentHashMap<>(1);
    afterEach(() -> cache.clear());

    return () -> {
      if (getCurrentSuiteBeingDeclared() == null) {
        return cache.computeIfAbsent(supplier, s -> s.get());
      }
      throw new IllegalStateException("Cannot use the value from let() in a suite declaration. "
          + "It may only be used in the context of a running spec.");
    };
  }

  /**
   * Supplier of results similar to {@link Supplier}, but may optionally throw checked exceptions.
   * Using {@link ThrowingSupplier} is more convenient for lambda functions since it requires less
   * exception handling.
   *
   * @see Supplier
   *
   * @param <T> The type of result that will be supplied
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> extends Supplier<T> {

    /**
     * Get a result.
     *
     * @return a result
     * @throws Throwable any uncaught Error or Exception
     */
    T getOrThrow() throws Throwable;

    @Override
    default T get() {
      try {
        return getOrThrow();
      } catch (final RuntimeException | Error unchecked) {
        throw unchecked;
      } catch (final Throwable checked) {
        throw new RuntimeException(checked);
      }
    }
  }


  private static final Deque<Suite> suiteStack = new ArrayDeque<>();

  private final Suite rootSuite;
  private final TestClass testClass;
  private final Box<Object> testObjectBox = new Box<>();

  /**
   * Main constructor called via reflection by the JUnit runtime.
   *
   * @param testClass The class file that defines the current suite
   *
   * @see org.junit.runner.Runner
   */
  public Spectrum(final Class<?> testClass) {
    this.rootSuite = Suite.rootSuite(Description.createSuiteDescription(testClass));
    this.testClass = new TestClass(testClass);
    beginDefinition(this.rootSuite, new ConstructorBlock(testClass, testObjectBox));
  }

  Spectrum(final Class<?> testClass, final Description description, final com.greghaskins.spectrum.Block definitionBlock) {
    this.rootSuite = Suite.rootSuite(description);
    this.testClass = new TestClass(testClass);
    beginDefinition(this.rootSuite, definitionBlock);
  }

  @Override
  public Description getDescription() {
    return this.rootSuite.getDescription();
  }

  @Override
  public void run(final RunNotifier notifier) {
    Statement runClassStatement = statementOf(() -> rootSuite.run(notifier, this::blockExecute));

    ThrowingSupplier<Void> wrapper = () -> addClassRulesAndExecute(runClassStatement);
    wrapper.get();
  }

  private void blockExecute(com.greghaskins.spectrum.Block toExecute, boolean isRoot) throws Throwable {
    if (!isRoot) {
      toExecute.run();
    } else {
      addMethodRulesAndExecute(statementOf(toExecute));
    }
  }

  private Statement statementOf(final com.greghaskins.spectrum.Block toExecute) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        toExecute.run();
      }
    };
  }

  private void addMethodRulesAndExecute(Statement base) throws Throwable {
    withTestRules(base).evaluate();
  }

  private Statement withTestRules(Statement base) {
    if (testObjectBox.get() == null) {
      return base;
    }
    List<TestRule> testRules = getTestRules(testObjectBox.get());
    return testRules.isEmpty() ? base :
        new RunRules(base, testRules, Description.createSuiteDescription(testClass.getJavaClass()));
  }

  /**
   * @param target the test case instance
   * @return a list of TestRules that should be applied when executing this
   *         test
   */
  protected List<TestRule> getTestRules(Object target) {
    List<TestRule> result = testClass.getAnnotatedMethodValues(target,
        Rule.class, TestRule.class);

    result.addAll(testClass.getAnnotatedFieldValues(target,
        Rule.class, TestRule.class));

    return result;
  }

  // Returns a void so it can be used with the catching supplier which rather helpfully
  // wraps checked exceptions when the outside world cannot receive Throwables
  private Void addClassRulesAndExecute(Statement statement) throws Throwable {
    addClassRules(statement).evaluate();
    return null;
  }

  private Statement addClassRules(Statement base) {
    List<TestRule> classRules = classRules();
    return classRules.isEmpty() ? base :
        new RunRules(base, classRules, getDescription());
  }

  private List<TestRule> classRules() {
    List<TestRule> result = testClass.getAnnotatedMethodValues(null, ClassRule.class, TestRule.class);
    result.addAll(testClass.getAnnotatedFieldValues(null, ClassRule.class, TestRule.class));
    return result;
  }

  private static synchronized void beginDefinition(final Suite suite,
      final com.greghaskins.spectrum.Block definitionBlock) {
    suiteStack.push(suite);
    try {
      definitionBlock.run();
    } catch (final Throwable error) {
      suite.removeAllChildren();
      it("encountered an error", () -> {
        throw error;
      });
    }
    suiteStack.pop();
  }

  private static synchronized Suite getCurrentSuiteBeingDeclared() {
    return suiteStack.peek();
  }

}
