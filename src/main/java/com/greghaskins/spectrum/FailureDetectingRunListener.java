package com.greghaskins.spectrum;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A listener to detect test failure. This is used by {@link Suite} to
 * detect whether to stop running its children if the children represent
 * a singular path of testing.
 */
class FailureDetectingRunListener extends RunListener {
  private boolean hasFailedYet = false;

  /**
   * Has the run failed since we've been listening.
   * @return whether any previous failures have been reported
   */
  boolean hasFailedYet() {
    return hasFailedYet;
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    super.testFailure(failure);
    hasFailedYet = true;
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    super.testAssumptionFailure(failure);
    hasFailedYet = true;
  }
}
