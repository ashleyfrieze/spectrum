package given.a.spec.with.bdd.annotation;

import static com.greghaskins.spectrum.BDDSyntax.*;
import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import java.util.stream.Collectors;

import com.greghaskins.spectrum.Spectrum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;

public class WhenDescribingTheSpec {

  private Description mainDescription;

  @Before
  public void before() throws Exception {
    final Description rootDescription =
        new Spectrum(getSpecWithNestedDescribeBlocks()).getDescription();
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
            "Then some sort of outcome(Scenario: a named scenario with)"));
  }

  private static Class<?> getSpecWithNestedDescribeBlocks() {
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

          });


        });
      }
    }

    return Spec.class;
  }

}
