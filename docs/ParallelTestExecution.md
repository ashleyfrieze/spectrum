# Parallel Test Execution

## Introduction

There are many reasons **not** to use Spectrum to execute tests in parallel. The main reason is that you can already use your test runner to spawn multi threaded tests. Surefire can be configured to run [multiple JVMs](http://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html) for testing and Gradle also has a [configuration property](https://docs.gradle.org/current/userguide/java_plugin.html#sec:test_execution).

Running tests inside your IDE, the above may not help, but optimising test execution for runs inside the IDE seems to be of only small advantage.

Drivers for declaring the individual test class to be run in parallel might include:

- The tests will only **work** if run in parallel sympathetically - i.e. with constraints over shared state
- The tests take a long time to execute and are largely bound by external resources
- The tests are innately parallel in nature and should declare this somehow

**Note:** running CPU intensive tests in parallel to optimise performance will give a gain related to the number of cores, but may not be best achieved with this feature set.

## Capabilities

Spectrum provides the following to help you parallelise your tests:

- A configurable test execution thread pool
- Tag a suite and its children for parallel execution
- Thread safety of `Variable`, `let` and `junitMixin`
- A thread safe way to share state with [`SharedVariable`](#shared-variable)
- Turnstiles that prevent critical sections of your test code running concurrently when they shouldn't
- A wrapper around test reporting to avoid confusing the test runner with interlaced test results

## Example

TODO

### Shared Variable

A shared variable is declared anywhere in your test class and can be used by any thread of any part of the test execution where it's visible. If this code only the `map` and `modify` functions, then all access to the object within the reference is strictly thread safe. As the real world is never that clean, there are `get` and `set` methods provided for cases where they are needed.

For more information see the JavaDoc in [SharedVariable.Java](../src/main/java/com.greghaskins.spectrum/SharedVariable.Java).