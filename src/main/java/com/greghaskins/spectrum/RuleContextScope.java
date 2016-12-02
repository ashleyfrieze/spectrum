package com.greghaskins.spectrum;

import java.util.function.Supplier;

/**
 * Container for a Rule Context to ensure it gets applied at the right level.
 */
class RuleContextScope<T> implements Supplier<RuleContext<T>> {
  private RuleContext<T> ruleContext;
  private int generation;

  static <T> RuleContextScope<T> ofRecursive(final RuleContext<T> context) {
    return new RuleContextScope<>(context, 0);
  }

  static <T> RuleContextScope<T> ofNonRecursive(final RuleContext<T> context) {
    return new RuleContextScope<>(context, -1);
  }

  private RuleContextScope(RuleContext<T> ruleContext, int generation) {
    this.ruleContext = ruleContext;
    this.generation = generation;
  }

  RuleContextScope<T> nextGeneration() {
    int nextGeneration = generation + 1;
    if (nextGeneration == 0) {
      return null;
    }

    return new RuleContextScope<>(ruleContext, nextGeneration);
  }

  /**
   * Is this a recursive rule?
   * @return true if the rule propagates down.
   */
  boolean appliesToLowerGeneration() {
    return generation >= 0;
  }

  /**
   * Is this rule set to run only at this generation?
   * @return true if the rule was created at this generation and runs method
   *         rules only at this point.
   */
  boolean appliesToThisGeneration() {
    return generation == -1;
  }

  /**
   * Is this the first generation of the rule?.
   * @return true if this is the first time the rules object/class appeared
   */
  boolean atFirstGeneration() {
    return generation < 1;
  }

  @Override
  public RuleContext<T> get() {
    return ruleContext;
  }
}
