package com.greghaskins.spectrum;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

interface Child {

  Description getDescription();

  void run(RunNotifier notifier, final BlockExecutor executor) throws Throwable;

  int testCount();

  void focus();

  void ignore();

}
