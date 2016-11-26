package given.a.spec.with.bdd.annotation;

import static com.greghaskins.spectrum.BddSyntax.and;
import static com.greghaskins.spectrum.BddSyntax.feature;
import static com.greghaskins.spectrum.BddSyntax.given;
import static com.greghaskins.spectrum.BddSyntax.scenario;
import static com.greghaskins.spectrum.BddSyntax.scenarioOutline;
import static com.greghaskins.spectrum.BddSyntax.then;
import static com.greghaskins.spectrum.BddSyntax.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

import com.greghaskins.spectrum.Spectrum;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhenDescribingTheSpec {
  private Description mainDescription;

  @Before
  public void before() throws Exception {
    final Description rootDescription =
        new Spectrum(getBddExampleSpec()).getDescription();
    this.mainDescription = rootDescription.getChildren().get(0);
  }

  @Test
  public void theTopLevelIsAFeature() throws Exception {
    assertThat(this.mainDescription.getDisplayName(),
        is("Feature: BDD semantics"));
  }

  @Test
  public void theNextLevelIsAScenario() throws Exception {
    assertThat(this.mainDescription.getChildren().get(0).getDisplayName(),
        is("Scenario: a named scenario with"));
  }

  @Test
  public void theScenarioHasGivenWhenThen() throws Exception {
    assertThat(this.mainDescription.getChildren().get(0).getChildren()
        .stream().map(Description::getDisplayName)
        .collect(Collectors.toList()),
        contains("Given some sort of given(Scenario: a named scenario with)",
            "When some sort of when(Scenario: a named scenario with)",
            "Then some sort of outcome(Scenario: a named scenario with)",
            "And an and on the end(Scenario: a named scenario with)"));
  }

  @Test
  public void scenarioOutlinesAreExpandedForAllVariants() throws Exception {
    final Description rootDescription =
        new Spectrum(getBddExampleSpecWithScenarioOutline()).getDescription();
    Description mainDescription = rootDescription.getChildren().get(0);

    assertThat(mainDescription.getChildren().get(0).getDisplayName(),
        is("Scenario: check Harry"));
    assertThat(mainDescription.getChildren().get(1).getDisplayName(),
        is("Scenario: check Potter"));
  }

  private static Class<?> getBddExampleSpec() {
    class Spec {
      {
        feature("BDD semantics", () -> {

          scenario("a named scenario with", () -> {

            given("some sort of given", () -> {
              Assert.assertTrue(true);
            });

            when("some sort of when", () -> {
              Assert.assertTrue(true);
            });

            then("some sort of outcome", () -> {
              Assert.assertTrue(true);
            });

            and("an and on the end", () -> {
              Assert.assertTrue(true);
            });

          });
        });
      }
    }

    return Spec.class;
  }

  private static Class<?> getBddExampleSpecWithScenarioOutline() {
    class Spec {
      {
        feature("BDD scenario outline supported", () -> {

          scenarioOutline("check", Stream.of("Harry", "Potter"),
              name -> {

                given("name is not null", () -> {
                  assertNotNull(name);
                });
              });
        });
      }
    }

    return Spec.class;
  }
}
