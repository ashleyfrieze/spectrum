package specs;

import static com.greghaskins.spectrum.BddSyntax.and;
import static com.greghaskins.spectrum.BddSyntax.feature;
import static com.greghaskins.spectrum.BddSyntax.given;
import static com.greghaskins.spectrum.BddSyntax.scenario;
import static com.greghaskins.spectrum.BddSyntax.scenarioOutline;
import static com.greghaskins.spectrum.BddSyntax.then;
import static com.greghaskins.spectrum.BddSyntax.when;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.Value;

import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Demonstrates the BDD syntax of Spectrum.
 */
@RunWith(Spectrum.class)
public class BddExampleSpecs {
  {
    feature("BDD", () -> {
      scenario("allow Gherkin like syntax", () -> {
        final AtomicInteger integer = new AtomicInteger();
        given("we start with a given", () -> {
          integer.set(12);
        });
        when("we have a when to execute the system", () -> {
          integer.incrementAndGet();
        });
        then("we can assert the outcome", () -> {
          assertThat(integer.get(), is(13));
        });
      });

      scenario("uses boxes within the scenario where data is passed between steps", () -> {
        Value<String> theData = new Value<>();

        given("the data is set", () -> {
          theData.set("Hello world");
        });

        when("the data is modified", () -> {
          theData.set(theData.get() + "!");
        });

        then("the data can be seen with the addition", () -> {
          assertThat(theData.get(), is("Hello world!"));
        });
      });

      scenario("uses default value from box", () -> {
        Value<String> theData = new Value<>("Hello world");

        given("the data is set correctly", () -> {
          assertThat(theData.get(), is("Hello world"));
        });

        when("the data is modified", () -> {
          theData.set(theData.get() + "!");
        });

        then("the data can be seen with the addition", () -> {
          assertThat(theData.get(), is("Hello world!"));
        });
      });

      scenarioOutline("rerun template scenario for", Stream.of("A", "B", "C"),
          letter -> {
            given("the letter " + letter, () -> {
              assumeThat(letter.length(), is(1));
            });
            then("it is not in the word Error", () -> {
              assertFalse("Error".contains(letter));
            });
          });

      scenarioOutline("outer parameterised scenario with", Stream.of("A", "B", "C"),
          letter1 -> {
            scenarioOutline("inner parameterised scenario with", Stream.of("Z", "X", "Y"),
                letter2 -> {
                  given("the first letter " + letter1, () -> {
                    assumeThat(letter1.length(), is(1));
                  });
                  and("the second letter " + letter2, () -> {
                    assumeThat(letter2.length(), is(1));
                  });
                  then("they are never the same", () -> {
                    assertNotEquals(letter1, letter2);
                  });
                });
          });
    });
  }
}
