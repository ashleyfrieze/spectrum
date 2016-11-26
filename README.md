Spectrum
========

[![Build Status](https://img.shields.io/travis/greghaskins/spectrum.svg)](https://travis-ci.org/greghaskins/spectrum) [![Coverage Status](https://img.shields.io/coveralls/greghaskins/spectrum.svg)](https://coveralls.io/r/greghaskins/spectrum) [![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE) [![Download](https://api.bintray.com/packages/greghaskins/maven/Spectrum/images/download.svg) ](https://bintray.com/greghaskins/maven/Spectrum/_latestVersion)

*A colorful BDD-style test runner for Java*

[Spectrum](https://github.com/greghaskins/spectrum) is inspired by the behavior-driven testing frameworks [Jasmine](https://jasmine.github.io/) and [RSpec](http://rspec.info/), bringing their expressive syntax and functional style to Java tests. It is a custom runner for [JUnit](http://junit.org/), so it works with many development and reporting tools out of the box.


## Example

> from [ExampleSpecs.java](src/test/java/specs/ExampleSpecs.java)

```java
@RunWith(Spectrum.class)
public class ExampleSpecs {
  {

    describe("A spec", () -> {

      final int foo = 1;

      it("is just a code block that verifies something", () -> {
        assertEquals(1, foo);
      });

      it("can use any assertion library you like", () -> {
        org.junit.Assert.assertEquals(1, foo);
        org.hamcrest.MatcherAssert.assertThat(true, is(true));
      });

      describe("nested inside a second describe", () -> {

        final int bar = 1;

        it("can reference both scopes as needed", () -> {
          assertThat(bar, is(equalTo(foo)));
        });

      });

      it("can have `it`s and `describe`s in any order", () -> {
        assertThat(foo, is(1));
      });

    });

    describe("A suite using beforeEach and afterEach", () -> {

      final List<String> items = new ArrayList<>();

      beforeEach(() -> {
        items.add("foo");
      });

      beforeEach(() -> {
        items.add("bar");
      });

      afterEach(() -> {
        items.clear();
      });

      it("runs the beforeEach() blocks in order", () -> {
        assertThat(items, contains("foo", "bar"));
        items.add("bogus");
      });

      it("runs them before every spec", () -> {
        assertThat(items, contains("foo", "bar"));
        items.add("bogus");
      });

      it("runs afterEach after every spec", () -> {
        assertThat(items, not(contains("bogus")));
      });

      describe("when nested", () -> {

        beforeEach(() -> {
          items.add("baz");
        });

        it("runs beforeEach and afterEach from inner and outer scopes", () -> {
          assertThat(items, contains("foo", "bar", "baz"));
        });

      });

    });

    describe("A suite using beforeAll", () -> {

      final List<Integer> numbers = new ArrayList<>();

      beforeAll(() -> {
        numbers.add(1);
      });

      it("sets the initial state before any specs run", () -> {
        assertThat(numbers, contains(1));
        numbers.add(2);
      });

      describe("and afterAll", () -> {

        afterAll(() -> {
          numbers.clear();
        });

        it("does not reset anything between tests", () -> {
          assertThat(numbers, contains(1, 2));
          numbers.add(3);
        });

        it("so proceed with caution; this *will* leak shared state across specs", () -> {
          assertThat(numbers, contains(1, 2, 3));
        });
      });

      it("cleans up after running all specs in the describe block", () -> {
        assertThat(numbers, is(empty()));
      });

    });

  }
}
```

### Screenshot

![Spectrum with Eclipse via JUnit](junit-screenshot.png)

### Selective running

#### Focused Specs Rspec style

You can focus the runner on particular spec with `fit` or a suite with `fdescribe` so that only those specs get executed.

> from [FocusedSpecs.java](src/test/java/specs/FocusedSpecs.java)

```java
describe("Focused specs", () -> {

  fit("is focused and will run", () -> {
    assertThat(true, is(true));
  });

  it("is not focused and will not run", () -> {
    throw new Exception();
  });

  fdescribe("a focused suite", () -> {

    it("will run", () -> {
      assertThat(true, is(true));
    });

    it("all its specs", () -> {
      assertThat(true, is(true));
    });
  });

  fdescribe("another focused suite, with focused and unfocused specs", () -> {

    fit("will run focused specs", () -> {
      assertThat(true, is(true));
    });

    it("ignores unfocused specs", () -> {
      throw new Exception();
    });
  });
});
```

#### Ignored Specs Rspec style
You can ignore a spec with `xit` or ignore all the specs in a suite with `xdescribe`.

> from [IgnoredSpecs.java](src/test/java/specs/IgnoredSpecs.java)

```java
describe("Ignored specs", () -> {

    xit("is ignored and will not run", () -> {
        throw new Exception();
    });

    it("is not ignored and will run", () -> {
        assertThat(true, is(true));
    });

    xdescribe("an ignored suite", () -> {

        it("will not run", () -> {
            throw new Exception();
        });

        describe("with nesting", () -> {
            it("all its specs", () -> {
                throw new Exception();
            });

            fit("including focused specs", () -> {
                throw new Exception();
            });
        });
    });
});
```

#### Ignored Specs JUnit style
You can ignore a section of the spec by inserting a call to `ignore`
in front of a `describe` or `it` call to ignore that individual item.
As `describe` implies a suite, the `ignore` will ignore all children of that
suite too.

```java
describe("Ignored specs", () -> {
    ignore();
    it("is ignored and will not run", () -> {
        throw new Exception();
    });

    it("is not ignored and will run", () -> {
        assertThat(true, is(true));
    });

    ignore();
    describe("an ignored suite", () -> {

        it("will not run", () -> {
            throw new Exception();
        });

        describe("with nesting", () -> {
            it("all its specs", () -> {
                throw new Exception();
            });
        });
    });
});
```
### Selective running with tags

You can tag things at any level of the hierarchy. You can have several tags.

```java
tag("wip","acceptance");
describe( ...
```

The tags can be used to either positively or negatively select tests to run. Inclusion and
exclusion rules can be set by:

* System property (See [SpectrumOptions.java](src/main/java/com/greghaskins/spectrum/SpectrumOptions.java))
* Annotation (See [SpectrumOptions.java](src/main/java/com/greghaskins/spectrum/SpectrumOptions.java))
* Function call - `includeTags` and `excludeTags` in the `Spectrum` class

If no inclusion/exclusions are provided, then the default is that all tags are allowed to run.
As soon as tags are marked for inclusion, then only those tags can run. If any tags are marked
for exclusion then they cannot be run.

Tags allow you run different categories of specs in different test runs, either through the
configuration of your build - usually with system property - or with hard coding in either the
class annotation of the test class containing your specs or within the specs themselves.

Example: temporarily making only WIP tests run in a test class

```java
  @RunWith(Spectrum.class)
  @SpectrumOptions(includeTags="wip")
  public TestClass {
     {
        tag("wip");
        describe("wip suite", () -> {
           // tests here are run
        });

        tag("someOtherTag");
        describe("some other suite", () -> {
           // these are not run
        });

        describe("untagged suite", () -> {
           // this suite is untagged but will still not run
           // as it's not tagged with what it needs to be included
        });
     }
  }
```

### Gherkin Syntax
The following Gherkin-like constructs are available (within the `BddSyntax` class):

* Feature - this is a suite, declared using `feature`
* Scenario - this is also a suite, declared with `scenario` which lives inside a feature
  * Scenarios can live inside other scenarios, though that's not encouraged
  * All previous steps in a scenario must have passed for the next to run - the scenario is aborted when a step fails
* ScenarioOutline - this is a templated scenario, declared with `scenarioOutline` allowing you to parameterise a scenario
  * You provide a stream of values, each of which is consumed by the definition of your scenario
  * n-dimensional test sets might be achieved by nested Scenario Outlines
* Given/When/Then/And - these are all just steps - the same level as `it` specs. They are declared with:
  * `given`
  * `when`
  * `then`
  * `and`

### Common Variable Initialization

The `let` helper function makes it easy to initialize common variables that are used in multiple specs. This also helps work around Java's restriction that closures can only reference `final` variables in the containing scope. Values are cached within a spec, and lazily re-initialized between specs as in [RSpec #let](http://rspec.info/documentation/3.5/rspec-core/RSpec/Core/MemoizedHelpers/ClassMethods.html#let-instance_method).

> from [LetSpecs.java](src/test/java/specs/LetSpecs.java)

```java
describe("The `let` helper function", () -> {

  final Supplier<List<String>> items = let(() -> new ArrayList<>(asList("foo", "bar")));

  it("is a way to supply a value for specs", () -> {
    assertThat(items.get(), contains("foo", "bar"));
  });

  it("caches the value so it doesn't get created multiple times for the same spec", () -> {
    assertThat(items.get(), is(sameInstance(items.get())));

    items.get().add("baz");
    items.get().add("blah");
    assertThat(items.get(), contains("foo", "bar", "baz", "blah"));
  });

  it("creates a fresh value for every spec", () -> {
    assertThat(items.get(), contains("foo", "bar"));
  });
});
```

Values can be held in a box using the `Value` class. This
allows the outcome of one spec to be used by one following. For
Rspec style specs this is most likely a lack of test isolation. For
steps in a Gherkin style test, this is how they will maintain
the state of the overall scenario test.

> from [BddExampleSpecs.java](src/test/java/specs/BddExampleSpecs.java)

```java
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
```

## Supported Features

The Spectrum API is designed to be familiar to Jasmine and RSpec users, while remaining compatible with JUnit. The features and behavior of those libraries help guide decisions on how Specturm should work, both for common scenarios and edge cases. (See [the discussion on #41](https://github.com/greghaskins/spectrum/pull/41#issuecomment-238729178) for an example of how this factors into design decisions.)

The main functions for defining a test are:

- `describe`
- `it`
- `beforeEach` / `afterEach`
- `beforeAll` / `afterAll`
- `fit` / `fdescribe`
- `xit` / `xdescribe`
- `let`

Spectrum also supports:

- Unlimited nesting of suites within suites
- Rigorous error handling and reporting when something unexpected goes wrong
- Compatibility with existing JUnit tools; no configuration required
- Mixing Spectrum tests and normal JUnit tests in the same project suite

### Non-Features

Unlike some BDD-style frameworks, Spectrum is _only_ a test runner. Assertions, expectations, mocks, and matchers are the purview of other libraries such as [Hamcrest](http://hamcrest.org/JavaHamcrest/), [AssertJ](http://joel-costigliola.github.io/assertj/), [Mockito](http://mockito.org/), or [plain JUnit](https://github.com/junit-team/junit4/wiki/Assertions).

## Getting Started

Spectrum is available as a [package on jCenter](https://bintray.com/greghaskins/maven/Spectrum/view), so make sure you have jCenter declared as a repository in your build config. Future inclusion in Maven Central (see [#12](https://github.com/greghaskins/spectrum/issues/12)) will make this even easier.

### Dependencies

 - JUnit 4
 - Java 8 (for your tests; systems under test can use older versions)

### Gradle

Make sure you have the jCenter repository in your [init script](https://docs.gradle.org/current/userguide/init_scripts.html) or project `build.gradle`:

```groovy
repositories {
    jcenter()
}
```

Then add the Spectrum dependency for your tests:

```groovy
dependencies {
  testCompile 'com.greghaskins:spectrum:1.0.0'
}

```

### Maven

Make sure you have the jCenter repository in your global `settings.xml` or project `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
```

Then add Spectrum as a dependency with `test` scope in your `pom.xml`:

```xml
<project>
  <dependencies>
    <dependency>
      <groupId>com.greghaskins</groupId>
      <artifactId>spectrum</artifactId>
      <version>1.0.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

## Can I Contribute?

Yes please! See [CONTRIBUTING.md](./CONTRIBUTING.md).
