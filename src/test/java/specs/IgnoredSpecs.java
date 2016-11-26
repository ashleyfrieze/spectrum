package specs;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.fdescribe;
import static com.greghaskins.spectrum.Spectrum.fit;
import static com.greghaskins.spectrum.Spectrum.ignore;
import static com.greghaskins.spectrum.Spectrum.it;
import static com.greghaskins.spectrum.Spectrum.let;
import static com.greghaskins.spectrum.Spectrum.xdescribe;
import static com.greghaskins.spectrum.Spectrum.xit;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.SpectrumHelper;

import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.function.Supplier;

@RunWith(Spectrum.class)
public class IgnoredSpecs {
  {
    describe("Ignored specs", () -> {
      it("are declared with `xit`", () -> {
        final Result result = SpectrumHelper.run(getSuiteWithIgnoredSpecs());
        assertThat(result.getFailureCount(), is(0));
      });

      it("ignores tests that are xit", () -> {
        final Result result = SpectrumHelper.run(getSuiteWithIgnoredSpecs());
        assertThat(result.getRunCount(), is(1));
        assertThat(result.getIgnoreCount(), is(2));
      });

      describe("with nesting", () -> {
        it("ignores only the nested spec", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithNestedIgnoredSpecs());
          assertThat(result.getFailureCount(), is(0));
          assertThat(result.getRunCount(), is(1));
          assertThat(result.getIgnoreCount(), is(1));
        });
      });
    });

    describe("Ignored suites", () -> {
      it("are declared with `xdescribe`", () -> {
        final Result result = SpectrumHelper.run(getSuiteWithIgnoredSubSuites());
        assertThat(result.getFailureCount(), is(0));
      });

      it("ignores tests that are xdescribe", () -> {
        final Result result = SpectrumHelper.run(getSuiteWithIgnoredSubSuites());
        assertThat(result.getRunCount(), is(1));
        assertThat(result.getIgnoreCount(), is(3));
      });

      describe("with nesting", () -> {
        it("cause specs in nested suites to also be ignored", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithNestedIgnoredSuites());
          assertThat(result.getFailureCount(), is(0));
          assertThat(result.getRunCount(), is(1));
          assertThat(result.getIgnoreCount(), is(1));
        });

        describe("and the nested suite and spec have a focus", () -> {
          it("ignores the focus", () -> {
            final Result result =
                SpectrumHelper.run(getSuiteWithNestedIgnoredSuitesAndFocusedSpecs());
            assertThat(result.getFailureCount(), is(0));
            assertThat(result.getRunCount(), is(1));
            assertThat(result.getIgnoreCount(), is(2));
          });
        });
      });
    });

    describe("Ignored specs example", () -> {
      final Supplier<Result> result = let(() -> SpectrumHelper.run(getIgnoredSpecsExample()));

      it("does not run ignored specs", () -> {
        assertThat(result.get().getFailureCount(), is(0));
      });
    });

    describe("Ignoring by method", () -> {
      describe("A single suite", () -> {
        it("is not executed", () -> {
          Result result = SpectrumHelper.run(getSingleIgnoredSuite());
          assertThat(result.getIgnoreCount(), is(1));
        });
      });
      describe("A nested suite", () -> {
        it("is not executed", () -> {
          Result result = SpectrumHelper.run(getNestedIgnoredSuite());
          assertThat(result.getIgnoreCount(), is(2));
        });
      });
    });
  }

  private static Class<?> getSuiteWithIgnoredSpecs() {
    class Suite {
      {

        describe("A spec that", () -> {

          it("is not ignored and will run", () -> {
            assertThat(true, is(true));
          });

          xit("is ignored and will not run", () -> {
            assertThat(true, is(false));
          });

          it("does not have a block and is ignored");
        });
      }
    }

    return Suite.class;
  }

  private static Class<?> getSuiteWithNestedIgnoredSpecs() {
    class Suite {
      {

        it("should run because it isn't ignored", () -> {
          assertThat(true, is(true));
        });

        describe("a nested context", () -> {
          xit("is ignored and will not run", () -> {
            assertThat(true, is(false));
          });
        });
      }
    }

    return Suite.class;
  }

  private static Class<?> getSuiteWithIgnoredSubSuites() {
    class Suite {
      {
        describe("an un-ignored suite", () -> {
          it("is not ignored", () -> {
            assertThat(true, is(true));
          });
        });

        xdescribe("ignored describe", () -> {
          it("will not run", () -> {
            assertThat(true, is(false));
          });

          it("will also not run", () -> {
            assertThat(true, is(false));
          });

          fit("will also not run a focused test", () -> {
            assertThat(true, is(false));
          });
        });

      }
    }

    return Suite.class;
  }

  private static Class<?> getSuiteWithNestedIgnoredSuitesAndFocusedSpecs() {
    class Suite {
      {

        describe("a nested context", () -> {
          describe("with a sub-suite", () -> {
            it("will run despite having a focused test", () -> {
              assertThat(true, is(true));
            });
          });
        });

        xdescribe("a nested ignored context", () -> {
          describe("with a sub-suite", () -> {
            fit("will not run focused test", () -> {
              assertThat(true, is(false));
            });
          });

          fdescribe("with focused sub-suite", () -> {
            it("will not run regular test", () -> {
              assertThat(true, is(false));
            });
          });
        });
      }
    }

    return Suite.class;
  }

  private static Class<?> getSuiteWithNestedIgnoredSuites() {
    class Suite {
      {

        describe("a nested context", () -> {
          describe("with a sub-suite", () -> {
            it("will run", () -> {
              assertThat(true, is(true));
            });
          });
        });

        xdescribe("a nested ignored context", () -> {
          describe("with a sub-suite", () -> {
            it("will not run", () -> {
              assertThat(true, is(false));
            });
          });
        });
      }
    }

    return Suite.class;
  }

  private static Class<?> getIgnoredSpecsExample() {
    class FocusedSpecsExample {
      {
        describe("Ignored specs", () -> {

          xit("with xit will not run", () -> {
            throw new Exception();
          });

          it("without a block are also ignored");

          it("is not ignored and will run", () -> {
            assertThat(true, is(true));
          });

          xdescribe("an ignored suite", () -> {

            it("will not run", () -> {
              throw new Exception();
            });

            describe("with nesting", () -> {
              it("will also ignore all its specs", () -> {
                throw new Exception();
              });

              fit("even focused specs are ignored", () -> {
                throw new Exception();
              });
            });
          });
        });
      }
    }

    return FocusedSpecsExample.class;
  }

  private static Class<?> getSingleIgnoredSuite() {
    class SingleIgnoredExample {
      {
        ignore();
        describe("This suite is ignored", () -> {
          it("should not be run", () -> {
            assertTrue(true);
          });
        });
      }
    }

    return SingleIgnoredExample.class;
  }

  private static Class<?> getNestedIgnoredSuite() {
    class SingleIgnoredExample {
      {
        ignore();
        describe("This suite is ignored", () -> {
          it("should not be run", () -> {
            assertTrue(true);
          });
          describe("A child suite is also ignored", () -> {
            it("should not be run", () -> {
              assertTrue(true);
            });
          });
        });
      }
    }

    return SingleIgnoredExample.class;
  }
}
