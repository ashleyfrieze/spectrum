package com.greghaskins.spectrum.internal;

import com.greghaskins.spectrum.Block;
import com.greghaskins.spectrum.internal.hooks.Hook;
import com.greghaskins.spectrum.internal.hooks.HookContext;
import com.greghaskins.spectrum.internal.hooks.HookContext.AppliesTo;
import com.greghaskins.spectrum.internal.hooks.HookContext.Precedence;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * DeclarationState - a singleton that tracks the hierarchy of suites that are currently being defined.
 * This allows all suite definition to be static calls.
 * The state is actually singleton within each thread, allowing Spectrum suites to be executed
 * by a parallelised test runner. This means you can use in-process parallel test discovery and execution with
 * Spectrum on top of its own parallel features.
 */
public final class DeclarationState {

  private static final ThreadLocal<DeclarationState> instance =
      ThreadLocal.withInitial(DeclarationState::new);

  public static DeclarationState instance() {
    return instance.get();
  }

  private final Deque<Suite> suiteStack = new ArrayDeque<>();

  private DeclarationState() {}

  public Suite getCurrentSuiteBeingDeclared() {
    return suiteStack.peek();
  }

  private int getCurrentDepth() {
    return suiteStack.size();
  }

  public void beginDeclaration(final Suite suite, final Block definitionBlock) {
    suiteStack.push(suite);

    try {
      definitionBlock.run();
    } catch (final Throwable error) {
      suite.removeAllChildren();
      suite.addSpec("encountered an error", () -> {
        throw error;
      });
    }
    suiteStack.pop();
  }

  public void addHook(final Hook hook, final AppliesTo appliesTo, final Precedence precedence) {
    addHook(new HookContext(hook, instance().getCurrentDepth(), appliesTo, precedence));
  }

  private void addHook(HookContext hook) {
    getCurrentSuiteBeingDeclared().addHook(hook);
  }


}
