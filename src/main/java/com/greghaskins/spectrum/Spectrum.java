package com.greghaskins.spectrum;

import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class Spectrum extends Runner {

    public static interface Block {
        void run() throws Throwable;
    }

    public static void describe(final String context, final Block block) {
        final Context newContext = new Context(Description.createSuiteDescription(context));
        enterContext(newContext, block);
    }

    public static void it(final String behavior, final Block block) {
        getCurrentContext().addTest(behavior, block);
    }

    public static void beforeEach(final Block block) {
        getCurrentContext().addTestSetup(block);
    }

    public static void afterEach(final Block block) {
        getCurrentContext().addTestTeardown(block);
    }

    public static void beforeAll(final Block block) {
        getCurrentContext().addContextSetup(block);
    }

    public static void afterAll(final Block block) {
        getCurrentContext().addContextTeardown(block);
    }

    public static <T> Value<T> value(@SuppressWarnings("unused") final Class<T> type){
        return new Value<T>(null);
    }

    public static <T> Value<T> value(final T startingValue){
        return new Value<T>(startingValue);
    }

    public static class Value<T> {
        public T value;

        private Value(final T value) {
            this.value = value;
        }
    }

    private static final Deque<Context> globalContexts = new ArrayDeque<Context>();
    static {
        globalContexts.push(new Context(Description.createSuiteDescription("Spectrum tests")));
    }

    private final Description description;
    private final Context rootContext;

    public Spectrum(final Class<?> testClass) {
        description = Description.createSuiteDescription(testClass);
        rootContext = new Context(description);
        enterContext(rootContext, new ConstructorBlock(testClass));
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        rootContext.execute(notifier);
    }

    private static void enterContext(final Context context, final Block block) {
        getCurrentContext().addChild(context);

        globalContexts.push(context);
        try {
            block.run();
        } catch (final Throwable e) {
            it("encountered an error", new FailingBlock(e));
        }
        globalContexts.pop();
    }

    private static Context getCurrentContext() {
        return globalContexts.peek();
    }

}
