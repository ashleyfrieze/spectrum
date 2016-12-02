package com.greghaskins.spectrum;

import static com.greghaskins.spectrum.RuleContextScope.ofNonRecursive;
import static com.greghaskins.spectrum.RuleContextScope.ofRecursive;
import static com.greghaskins.spectrum.RuleExecution.runWithClassBlockRules;
import static com.greghaskins.spectrum.RuleExecution.runWithRules;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class Suite implements Parent, Child {

  private final SetupBlock beforeAll = new SetupBlock();
  private final TeardownBlock afterAll = new TeardownBlock();

  private final SetupBlock beforeEach = new SetupBlock();
  private final TeardownBlock afterEach = new TeardownBlock();

  private ThrowingConsumer<Block> aroundEach = Block::run;
  private ThrowingConsumer<Block> aroundAll = Block::run;

  protected final List<Child> children = new ArrayList<>();
  private final Set<Child> focusedChildren = new HashSet<>();

  private final LinkedList<RuleContextScope> rulesToApply = new LinkedList<>();

  private final ChildRunner childRunner;

  private final Description description;
  private final Parent parent;
  private boolean ignored;

  private final TaggingState tagging;
  private PreConditions preconditions = PreConditions.Factory.defaultPreConditions();
  private Set<String> namesUsed = new HashSet<>();

  /**
   * The strategy for running the children within the suite.
   */
  @FunctionalInterface
  interface ChildRunner {
    void runChildren(final Suite suite, final RunNotifier notifier);
  }

  static Suite rootSuite(final Description description) {
    return new Suite(description, Parent.NONE, Suite::defaultChildRunner, new TaggingState());
  }

  /**
   * Constructs a suite.
   *
   * @param description the JUnit description
   * @param parent parent item
   * @param childRunner which child running strategy to use - this will normally be
   *             {@link #defaultChildRunner(Suite, RunNotifier)} which runs them all
   *             but can be substituted for a strategy that ignores all specs
   *             after a test failure e.g.
   *             {@link CompositeTest#abortOnFailureChildRunner(Suite, RunNotifier)}
   * @param taggingState the state of tagging inherited from the parent
   */
  protected Suite(final Description description, final Parent parent, final ChildRunner childRunner,
      final TaggingState taggingState) {
    this.description = description;
    this.parent = parent;
    this.ignored = parent.isIgnored();
    this.childRunner = childRunner;
    this.tagging = taggingState;
  }

  Suite addSuite(final String name) {
    return addSuite(name, Suite::defaultChildRunner);
  }

  Suite addSuite(final String name, final ChildRunner childRunner) {
    final Suite suite =
        new Suite(Description.createSuiteDescription(sanitise(name)), this, childRunner,
            this.tagging.clone());
    completeSuiteDefinitionAndAdd(suite);

    return suite;
  }

  void completeSuiteDefinitionAndAdd(final Suite suite) {
    suite.beforeAll.addBlock(this.beforeAll);
    suite.beforeEach.addBlock(this.beforeEach);
    suite.afterEach.addBlock(this.afterEach);
    suite.aroundEach(this.aroundEach);
    rulesToApply.stream()
        .map(RuleContextScope::nextGeneration)
        .filter(scope -> scope != null)
        .forEach(suite.rulesToApply::add);
    addChild(suite);
  }

  Suite addCompositeTest(final String name) {
    final CompositeTest suite =
        new CompositeTest(Description.createSuiteDescription(name), this, this.tagging.clone());
    completeSuiteDefinitionAndAdd(suite);

    return suite;
  }

  Child addSpec(final String name, final Block block) {
    final Child spec = createSpec(name, block);
    addChild(spec);

    return spec;
  }

  private Child createSpec(final String name, final Block block) {
    final Description specDescription =
        Description.createTestDescription(this.description.getClassName(), sanitise(name));

    final NotifyingBlock specBlockInContext = (description, notifier) -> {
      try {
        this.beforeAll.run();
      } catch (final Throwable exception) {
        notifier.fireTestFailure(new Failure(description, exception));
        return;
      }

      NotifyingBlock.run(description, notifier, () -> {
        Variable<Boolean> blockWasRun = new Variable<>(false);
        this.aroundEach.accept(() -> {
          blockWasRun.set(true);

          NotifyingBlock.run(description, notifier, () -> {
            this.beforeEach.run();
            block.run();
          });

          this.afterEach.run(description, notifier);
        });
        if (!blockWasRun.get()) {
          throw new RuntimeException("aroundEach did not run the block");
        }
      });
    };

    PreConditionBlock preConditionBlock =
        PreConditionBlock.with(this.preconditions.forChild(), block);

    return new Spec(specDescription, specBlockInContext, this).applyPreConditions(preConditionBlock,
        this.tagging);
  }

  private void addChild(final Child child) {
    this.children.add(child);
  }

  void beforeAll(final Block block) {
    this.beforeAll.addBlock(new IdempotentBlock(block));
  }

  void afterAll(final Block block) {
    this.afterAll.addBlock(block);
  }

  void beforeEach(final Block block) {
    this.beforeEach.addBlock(block);
  }

  void afterEach(final Block block) {
    this.afterEach.addBlock(block);
  }

  /**
   * Set the suite to require certain tags of all tests below.
   *
   * @param tags required tags - suites must have at least one of these if any are specified
   */
  void includeTags(final String... tags) {
    this.tagging.include(tags);
  }

  /**
   * Set the suite to exclude certain tags of all tests below.
   *
   * @param tags excluded tags - suites and specs must not have any of these if any are specified
   */
  void excludeTags(final String... tags) {
    this.tagging.exclude(tags);
  }

  void applyPreConditions(Block block) {
    this.preconditions = Child.findApplicablePreconditions(block);
    applyPreConditions(block, this.tagging);
  }

  <T> Supplier<T> applyRules(final Class<T> ruleClass, final boolean recursive) {
    final RuleContext<T> context = new RuleContext<>(ruleClass);
    // as rule execution wraps the method execution, the rules must be stored
    // in reverse order so the wrapping ends up in the correct order
    rulesToApply.addFirst(recursive ? ofRecursive(context) : ofNonRecursive(context));

    return context;
  }

  /**
   * Add a known test object as the first object to get its rules executed.
   * @param object test object
   */
  void insertTestObjectRules(Object object) {
    if (object != null) {
      applyRecursively(ofRecursive(new RuleContext<Object>(object)));
    }
  }

  void applyRecursively(RuleContextScope scope) {
    rulesToApply.add(scope);
    children.stream()
        .filter(child -> !(child instanceof Atomic))
        .filter(child -> child instanceof Suite)
        .map(child -> (Suite) child)
        .forEach(parent -> parent.applyRecursively(scope));
  }

  @Override
  public void focus(final Child child) {
    this.focusedChildren.add(child);
    focus();
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

  @Override
  public boolean isIgnored() {
    return this.ignored;
  }

  @Override
  public void run(final RunNotifier notifier) {
    if (testCount() == 0) {
      notifier.fireTestIgnored(this.description);
      NotifyingBlock.run(getDescription(), notifier, () -> runChildren(notifier));
    } else {
      runSuite(notifier);
    }
  }

  private void runSuite(final RunNotifier notifier) {
    Variable<Boolean> blockWasCalled = new Variable<>(false);

    NotifyingBlock.run(getDescription(), notifier, () -> this.aroundAll.accept(() -> {
      blockWasCalled.set(true);
      NotifyingBlock.run(getDescription(), notifier, () -> runChildren(notifier));
      runAfterAll(notifier);
    }));

    if (!blockWasCalled.get()) {
      RuntimeException exception = new RuntimeException("aroundAll did not run the block");
      notifier.fireTestFailure(new Failure(this.description, exception));
    }
  }

  private void runChildren(final RunNotifier notifier) throws Throwable {
    runWithClassBlockRules(filteredRules(RuleContextScope::atFirstGeneration),
        () -> this.childRunner.runChildren(this, notifier), getDescription());
  }

  private List<RuleContext> filteredRules(Predicate<RuleContextScope> predicate) {
    return rulesToApply.stream()
        .filter(predicate)
        .map(RuleContextScope::get)
        .collect(Collectors.toList());
  }

  protected void runChild(final Child child, final RunNotifier notifier) {
    if (this.focusedChildren.isEmpty() || this.focusedChildren.contains(child)) {
      NotifyingBlock.run(getDescription(), notifier,
          () -> runWithRules(filteredRules(RuleContextScope::appliesToThisGeneration),
              child, () -> runChildWithRules(child, notifier)));
    } else {
      notifier.fireTestIgnored(child.getDescription());
    }
  }

  private void runChildWithRules(final Child child, final RunNotifier notifier) throws Throwable {
    if (child instanceof Atomic) {
      runWithRules(filteredRules(RuleContextScope::appliesToLowerGeneration),
          child, notifier);
    } else {
      child.run(notifier);
    }
  }

  private void runAfterAll(final RunNotifier notifier) {
    this.afterAll.run(this.description, notifier);
  }

  @Override
  public Description getDescription() {
    final Description copy = this.description.childlessCopy();
    this.children.forEach((child) -> copy.addChild(child.getDescription()));

    return copy;
  }

  @Override
  public int testCount() {
    return this.children.stream().mapToInt(Child::testCount).sum();
  }

  void removeAllChildren() {
    this.children.clear();
  }

  private static void defaultChildRunner(final Suite suite, final RunNotifier runNotifier) {
    suite.children.forEach((child) -> suite.runChild(child, runNotifier));
  }

  private String sanitise(final String name) {
    String sanitised = name.replaceAll("\\(", "[")
        .replaceAll("\\)", "]");

    int suffix = 1;
    String deDuplicated = sanitised;
    while (this.namesUsed.contains(deDuplicated)) {
      deDuplicated = sanitised + "_" + suffix++;
    }
    this.namesUsed.add(deDuplicated);

    return deDuplicated;
  }

  void aroundEach(ThrowingConsumer<Block> consumer) {
    ThrowingConsumer<Block> outerAroundEach = this.aroundEach;
    this.aroundEach = block -> {
      outerAroundEach.accept(() -> {
        consumer.accept(block);
      });

    };
  }

  void aroundAll(ThrowingConsumer<Block> consumer) {
    this.aroundAll = consumer;
  }
}
