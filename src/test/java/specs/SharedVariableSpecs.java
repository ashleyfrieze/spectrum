package specs;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.greghaskins.spectrum.SharedVariable;
import com.greghaskins.spectrum.Spectrum;
import org.junit.runner.RunWith;

/**
 * Tests for {@link com.greghaskins.spectrum.SharedVariable}
 */
@RunWith(Spectrum.class)
public class SharedVariableSpecs {{
	describe("SharedVariable", () -> {
		it("starts with an initial value", () -> {
			SharedVariable<String> variable = new SharedVariable<>("Initial");
			assertEquals("Initial", variable.get());
		});

		it("can have its value re-set", () -> {
			SharedVariable<String> variable = new SharedVariable<>("Initial");
			variable.set("Non initial");
			assertEquals("Non initial", variable.get());
		});

		it("can have a value calculated from it", () -> {
			SharedVariable<List<String>> sharedList = new SharedVariable<>(asList("Hello", "World"));
			assertEquals("World", sharedList.map(list -> list.get(1)));
		});

		it("can be modified with a functor", () -> {
			// this list is modifiable
			SharedVariable<List<String>> sharedList =
				new SharedVariable<>(new ArrayList<>(asList("Hello", "World")));

			sharedList.modify(list -> list.add("Universe"));
			assertEquals("Universe", sharedList.map(list -> list.get(2)));
		});

		it("can be set from another shared variable", () -> {
			SharedVariable<Integer> int1 = new SharedVariable<>(1);
			SharedVariable<Integer> int2 = new SharedVariable<>(999);

			int2.set(int1);

			assertEquals(1, int2.get().intValue());
		});
	});
}}
