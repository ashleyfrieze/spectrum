package specs;

import static com.greghaskins.spectrum.BDDSyntax.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import com.greghaskins.spectrum.Box;
import com.greghaskins.spectrum.Spectrum;
import org.junit.runner.RunWith;

/**
 * Demonstrates the BDD syntax of Spectrum
 */
@RunWith(Spectrum.class)
public class BDDExampleSpecs {{
	feature("BDD", ()-> {
		scenario("allow Gherkin like syntax", ()-> {
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

		scenario("uses boxes within the scenariowhere data is passed between steps", () -> {
			Box<String> theData = new Box<>();

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
	});
}}
