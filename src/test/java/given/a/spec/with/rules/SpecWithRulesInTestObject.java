package given.a.spec.with.rules;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.greghaskins.spectrum.Spectrum;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import rule.ExampleMethodRule;
import rule.ExampleRule;

@RunWith(Spectrum.class)
public class SpecWithRulesInTestObject {
  @ClassRule
  public static ExampleRule classRule = new ExampleRule();

  @Rule
  public ExampleMethodRule methodRule = new ExampleMethodRule();

  {
    describe("A spec with a class rule", () -> {
      it("will have executed the class rule before entering any step", () -> {
        assertThat(classRule.getCount(), is(1));
      });
      it("will have executed the method rule before the it block", () -> {
        assertThat(methodRule.getCount(), is(2));
      });
      it("will have executed the method rule again before the it block", () -> {
        assertThat(methodRule.getCount(), is(3));
      });
    });

    describe("A second invocation within a spec with a class rule", () -> {
      it("will have not executed the class rule again", () -> {
        assertThat(classRule.getCount(), is(1));
      });
      it("will have executed the method rule again", () -> {
        assertThat(methodRule.getCount(), is(5));
      });
    });
  }
}
