package specs;

import static com.greghaskins.spectrum.Spectrum.*;
import static com.greghaskins.spectrum.dsl.specification.Specification.aroundAll;
import static com.greghaskins.spectrum.dsl.specification.Specification.aroundEach;

import com.greghaskins.spectrum.Spectrum;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@RunWith(Spectrum.class)
public class BeforeEachSpecs {
	interface SomeService {
		void doSomething();
	}

	private SomeService someService;

	{
		describe("suite1", () -> {
			beforeEach(() -> {
				someService.doSomething();
				//block.run();
			});

			describe("application", () -> {
				it("fails somehow", () -> {

				});
			});
		});

		describe("suite 2", () ->{
			it("is fine", () -> {});

			it("is igored");
		});

	}
}
