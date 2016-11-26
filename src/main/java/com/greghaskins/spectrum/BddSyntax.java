package com.greghaskins.spectrum;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.describeChain;
import static com.greghaskins.spectrum.Spectrum.it;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A translation from Spectrum describe/it to Gherkin like Feature/Scenario/Given/When/Then syntax
 * Note - the beforeEach and afterEach will still be executed between given/when/then steps which
 * may not make sense in many situations.
 */
public class BddSyntax {
  /**
   * Describes a feature of the system. A feature may have many scenarios.
   * @param featureName name of feature
   * @param block the contents of the feature
   */
  public static void feature(final String featureName, final Block block) {
    describe("Feature: " + featureName, block);
  }

  /**
   * Describes a scenario of the system. These can be at root level, though are best grouped
   * inside features.
   * @param scenarioName name of scenario
   * @param block the contents of the scenario - given/when/then steps
   */
  public static void scenario(final String scenarioName, final Block block) {
    describeChain("Scenario: " + scenarioName, block);
  }

  /**
   * Describes a class of scenarios which are parameterised by data.
   * @param scenarioName name of scenario
   * @param variants the input data - there will be a scenario for each
   * @param block the internals of the scenario, which must consume and use each variant
   */
  public static <T> void scenarioOutline(final String scenarioName,
      final Stream<T> variants, final Consumer<T> block) {
    variants.forEach(variant -> describeChain("Scenario: " + scenarioName + " " + variant,
        () -> block.accept(variant)));
  }

  /**
   * A gherkin like given block.
   * @param behavior the behaviour to associate with the precondition
   * @param block how to execute that precondition
   */
  public static void given(final String behavior, final Block block) {
    it("Given " + behavior, block);
  }

  /**
   * A gherkin like when block.
   * @param behavior the behaviour to associate with the manipulation of the system under test
   * @param block how to execute that behaviour
   */
  public static void when(final String behavior, final Block block) {
    it("When " + behavior, block);
  }

  /**
   * A gherkin like then block.
   * @param behavior the behaviour to associate with the postcondition
   * @param block how to execute that postcondition
   */
  public static void then(final String behavior, final Block block) {
    it("Then " + behavior, block);
  }

  /**
   * An and block.
   * @param behavior what we would like to describe as an and
   * @param block how to achieve the and block
   */
  public static void and(final String behavior, final Block block) {
    it("And " + behavior, block);
  }

}
