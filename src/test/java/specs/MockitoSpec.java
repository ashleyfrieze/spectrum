package specs;

import static com.greghaskins.spectrum.Spectrum.beforeEach;
import static com.greghaskins.spectrum.Spectrum.describe;

import com.greghaskins.spectrum.Spectrum;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.greghaskins.spectrum.Spectrum.it;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

/**
 * This uses the mockito library
 */
@RunWith(Spectrum.class)
public class MockitoSpec {
	@Mock
	private SomeInterface mockInterface;

	@InjectMocks
	private SomeClass objectUnderTest;

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	{
		describe("A suite which needs mockito", () -> {
			beforeEach(() -> {
				given(mockInterface.getInput()).willReturn("Hello world");
			});

			it("can use the mocks", () -> {
				assertThat(objectUnderTest.getResult(), is("Hello world"));
			});

			it("can use the mocks again", () -> {
				assertThat(objectUnderTest.getResult(), is("Hello world"));
			});

			it("gets a fresh mock each time", () -> {
				verify(mockInterface, never()).getInput();
			});

		});
	}
}
