package com.greghaskins.spectrum;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * Wraps up the mixing in of JUnit rules with Spectrum execution.
 */
interface RuleExecution {
  /**
   * Apply the rules from the list of rules to the child that will be executed. Then execute.
   * @param rulesToApply list of rules which goes from most recent to least
   * @param child the child object to be executed
   * @param notifier the run notifier
   * @throws Throwable on error
   */
  static void runWithRules(final List<RuleContext> rulesToApply, final Child child,
      final RunNotifier notifier) throws Throwable {
    runWithRules(rulesToApply, child, () -> child.run(notifier));
  }

  /**
   * Apply the rules from the list of rules to the child that will be executed. Then execute.
   * @param rulesToApply list of rules which goes from most recent to least
   * @param child the child object to be executed
   * @param block the block of execution
   * @throws Throwable on error
   */
  static void runWithRules(final List<RuleContext> rulesToApply, final Child child,
      final Block block) throws Throwable {
    Statement base = statementOf(block);

    for (RuleContext context : rulesToApply) {
      base = context.decorate(base, child.getDescription());
    }

    base.evaluate();
  }

  /**
   * Apply the rules from the list of rules to the child that will be executed. Then execute.
   * @param rulesToApply list of rules which goes from most recent to least
   * @param block the block of execution
   * @param description description of the suite undertaking this
   * @throws Throwable on error
   */
  static void runWithClassBlockRules(final List<RuleContext> rulesToApply,
      final Block block, final Description description) throws Throwable {
    Statement base = statementOf(block);

    for (RuleContext context : rulesToApply) {
      base = context.withClassBlock(base, description);
    }

    base.evaluate();
  }

  /**
   * Wrap a {@link Block} as a {@link Statement} for JUnit purposes.
   * @param toExecute block that will be running inside the statement
   * @return statement encapsulating the work
   */
  static Statement statementOf(final Block toExecute) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        toExecute.run();
      }
    };
  }
}
