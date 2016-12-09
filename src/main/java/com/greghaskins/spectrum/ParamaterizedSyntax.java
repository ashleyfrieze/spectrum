package com.greghaskins.spectrum;

import com.greghaskins.spectrum.internal.parameterized.Example;
import com.greghaskins.spectrum.internal.parameterized.ParameterizedDefinitionBlock.FourArgBlock;
import com.greghaskins.spectrum.internal.parameterized.ParameterizedDefinitionBlock.OneArgBlock;
import com.greghaskins.spectrum.internal.parameterized.ParameterizedDefinitionBlock.ThreeArgBlock;
import com.greghaskins.spectrum.internal.parameterized.ParameterizedDefinitionBlock.TwoArgBlock;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Common syntax for parameterization.
 */
public interface ParamaterizedSyntax {
  static <T> void describe(final String name, final T block,
      final Stream<Example<T>> examples) {
    Spectrum.describe(name, () -> {
      Spectrum.describe("Examples:", () -> {
        examples.forEach(example -> {
          Spectrum.describe(example.toString(), () -> example.runDeclaration(block));
        });
      });
    });
  }


  @SafeVarargs
  static <T> Stream<Example<T>> withExamples(Example<T>... examples) {
    return Arrays.stream(examples);
  }

  static <T1, T2> Example<OneArgBlock<T1>> example(T1 t1) {
    return new Example<>(block -> block.run(t1), t1);
  }

  static <T1, T2> Example<TwoArgBlock<T1, T2>> example(T1 t1, T2 t2) {
    return new Example<>(block -> block.run(t1, t2), t1, t2);
  }

  static <T1, T2, T3> Example<ThreeArgBlock<T1, T2, T3>> example(T1 t1, T2 t2, T3 t3) {
    return new Example<>(block -> block.run(t1, t2, t3), t1, t2, t3);
  }

  static <T1, T2, T3, T4> Example<FourArgBlock<T1, T2, T3, T4>> example(T1 t1, T2 t2, T3 t3,
      T4 t4) {
    return new Example<>(block -> block.run(t1, t2, t3, t4), t1, t2, t3, t4);
  }
}
