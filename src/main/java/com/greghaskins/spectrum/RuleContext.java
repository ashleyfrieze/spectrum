package com.greghaskins.spectrum;

import static com.greghaskins.spectrum.internal.junit.StubJUnitFrameworkMethod.stubFrameworkMethod;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.MethodRule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tracks a rule that must be applied to all descendants of a suite.
 */
public class RuleContext<T> implements Supplier<T> {
  private final Class<T> ruleClass;
  private final TestClass testClass;
  private T currentTestObject;
  private final boolean constructEveryTime;
  boolean hasRunClassRule = false;

  RuleContext(final Class<T> ruleClass) {
    this.ruleClass = ruleClass;
    this.testClass = new TestClass(ruleClass);
    this.constructEveryTime = true;
  }

  RuleContext(final T object) {
    this.ruleClass = (Class<T>) object.getClass();
    this.testClass = new TestClass(this.ruleClass);
    this.currentTestObject = object;
    this.constructEveryTime = false;
  }

  @Override
  public T get() {
    return currentTestObject;
  }

  /**
   * Add the method and test rules execution around a test method statement.
   * @param base the base statement
   * @param description of the child
   * @return the statement to use to execute the child within the rules
   * @throws Throwable on error
   */
  Statement decorate(final Statement base, final Description description) throws Throwable {

    if (constructEveryTime) {
      constructTestObject();
    }

    return withTestRules(getTestRules(currentTestObject),
        withMethodRules(base, getMethodRules(currentTestObject)), description);
  }

  @SuppressWarnings("unchecked")
  private void constructTestObject() throws Throwable {
    ConstructorBlock constructor = new ConstructorBlock(ruleClass);
    constructor.run();
    currentTestObject = (T) constructor.get();
  }

  private Statement withMethodRules(final Statement base, final List<MethodRule> methodRules) {
    FrameworkMethod method = stubFrameworkMethod();

    return decorateWithMethodRules(base, methodRules, method);
  }

  private Statement decorateWithMethodRules(final Statement base,
      final List<MethodRule> methodRules,
      final FrameworkMethod method) {
    Statement result = base;
    for (MethodRule each : methodRules) {
      result = each.apply(result, method, currentTestObject);
    }

    return result;
  }

  private Statement withTestRules(final List<TestRule> testRules, final Statement statement,
      final Description childDescription) {
    return testRules.isEmpty() ? statement : new RunRules(statement, testRules, childDescription);
  }

  /**
   * Find the method rules within the test class mixin.
   * @param target the test case instance
   * @return a list of TestRules that should be applied when executing this
   *         test
   */
  protected List<MethodRule> getMethodRules(final Object target) {
    return Stream.concat(
        testClass.getAnnotatedMethodValues(target, Rule.class, MethodRule.class).stream(),
        testClass.getAnnotatedFieldValues(target, Rule.class, MethodRule.class).stream())
        .collect(Collectors.toList());
  }

  /**
   * Find the test rules within the test mixin.
   * @param target the test case instance
   * @return a list of TestRules that should be applied when executing this
   *         test
   */
  protected List<TestRule> getTestRules(final Object target) {
    return Stream.concat(
        testClass.getAnnotatedMethodValues(target, Rule.class, TestRule.class).stream(),
        testClass.getAnnotatedFieldValues(target, Rule.class, TestRule.class).stream())
        .collect(Collectors.toList());
  }

  Statement withClassBlock(final Statement base, final Description description) {
    return withClassRules(withAfterClasses(withBeforeClasses(base)), description);
  }

  // In the case of multi-threaded execution, this will prevent two threads from
  // executing the same class rule.
  private synchronized Statement withClassRules(final Statement base,
      final Description description) {
    if (hasRunClassRule) {
      return base;
    }
    hasRunClassRule = true;
    List<TestRule> classRules = classRules();

    return classRules.isEmpty() ? base : new RunRules(base, classRules, description);
  }

  private Statement withAfterClasses(final Statement base) {
    List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterClass.class);

    return afters.isEmpty() ? base : new RunAfters(base, afters, null);
  }

  private Statement withBeforeClasses(final Statement base) {
    List<FrameworkMethod> befores = testClass.getAnnotatedMethods(BeforeClass.class);

    return befores.isEmpty() ? base : new RunBefores(base, befores, null);
  }

  private List<TestRule> classRules() {
    return Stream.concat(
        testClass.getAnnotatedMethodValues(null, ClassRule.class, TestRule.class).stream(),
        testClass.getAnnotatedFieldValues(null, ClassRule.class, TestRule.class).stream())
        .collect(Collectors.toList());
  }
}
