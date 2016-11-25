package com.greghaskins.spectrum;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

final class Spec implements Child {

  private final NotifyingBlock block;
  private final Description description;
  private final Parent parent;
  private boolean ignored = false;

  Spec(final Description description, final NotifyingBlock block, final Parent parent) {
    this.description = description;
    this.block = block;
    this.parent = parent;
    this.ignored = parent.isIgnored();
  }

  @Override
  public Description getDescription() {
    return this.description;
  }

  @Override
  public void run(final RunNotifier notifier, final BlockExecutor executor) throws Throwable {
    if (this.ignored) {
      notifier.fireTestIgnored(this.description);
      return;
    }

    executor.execute(() -> {notifier.fireTestStarted(this.description);
      this.block.run(this.description, notifier);
      notifier.fireTestFinished(this.description);
    }, false);

  }

  @Override
  public int testCount() {
    return 1;
  }

  @Override
  public void focus() {
    if (this.ignored) {
      return;
    }

    this.parent.focus(this);
  }

  @Override
  public void ignore() {
    this.ignored = true;
  }
}
