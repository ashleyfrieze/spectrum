package specs;

import static com.greghaskins.spectrum.GherkinSyntax.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.Variable;
import org.junit.runner.RunWith;

/**
 * Trying out Scenario outline.
 */
@RunWith(Spectrum.class)
public class CukeEaterSpec {
	// a cuke eater which uses more than one type of object in its interface
	static class CukeEater {
		private int total;

		public void buy(int cukes) {
			total += cukes;
		}

		public void eat(int cukes) {
			total -= cukes;
		}

		public String remaining() {
			return Integer.toString(total);
		}
	}

	{
		feature("Cuke eating", () -> {
			scenario("no cukes", () -> {
				Variable<CukeEater> cukeEater = new Variable<>();
				given("a default cuke eater", () -> {
					cukeEater.set(new CukeEater());
				});
				then("there are no cukes", ()-> {
					assertThat(cukeEater.get().remaining(), is("0"));
				});
			});

			scenarioOutline("Cuke eating", (buy, eat, remaining) -> {
				Variable<CukeEater> cukeEater = new Variable<>();
				given("a default cuke eater", () -> {
					cukeEater.set(new CukeEater());
				});
				and("we buy " + buy + " cukes", () -> {
					cukeEater.get().buy(buy);
				});
				when("we eat " + eat + " cukes", () -> {
					cukeEater.get().eat(eat);
				});
				then("there are " + remaining + " cukes", ()-> {
					assertThat(cukeEater.get().remaining(), is(remaining));
				});
			}, withExamples(
				example(12, 5, "7"),
				example(20, 5, "15")
			));
		});

	}
}
