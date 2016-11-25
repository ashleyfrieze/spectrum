package given.a.spec.with.rules;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.greghaskins.spectrum.Spectrum;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import rule.ExampleRule;

@RunWith(Spectrum.class)
public class SpecWithRules {
	@ClassRule
	public static ExampleRule classRule = new ExampleRule();

	@Rule
	public ExampleRule methodRule = new ExampleRule();

	{
		describe("A spec with a class rule", () -> {
			it("will have executed the class rule before entering any step", () -> {
				assertThat(classRule.getCount(), is(1));
			});
			it("will have executed the method rule before the describe block", () -> {
				assertThat(methodRule.getCount(), is(1));
			});
			it("will have only executed the method rule once before the describe block", () -> {
				assertThat(methodRule.getCount(), is(1));
			});
		});

		describe("A second invocation within a spec with a class rule", () -> {
			it("will have not executed the class rule again", () -> {
				assertThat(classRule.getCount(), is(1));
			});
			it("will have executed the method rule again in the second describe block", () -> {
				assertThat(methodRule.getCount(), is(2));
			});
		});
	}
}
