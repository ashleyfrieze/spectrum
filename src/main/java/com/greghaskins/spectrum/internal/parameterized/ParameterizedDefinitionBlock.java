package com.greghaskins.spectrum.internal.parameterized;

/**
 * The definition of a parameterized definition block.
 */
public interface ParameterizedDefinitionBlock<T> {
  @FunctionalInterface
  interface OneArgBlock<T> extends ParameterizedDefinitionBlock<OneArgBlock<T>> {
    void run(T arg0);
  }

  @FunctionalInterface
  interface TwoArgBlock<T1, T2> extends ParameterizedDefinitionBlock<TwoArgBlock<T1, T2>> {
    void run(T1 arg0, T2 arg1);
  }

  @FunctionalInterface
  interface ThreeArgBlock<T1, T2, T3>
      extends ParameterizedDefinitionBlock<ThreeArgBlock<T1, T2, T3>> {
    void run(T1 arg0, T2 arg1, T3 arg2);
  }

  @FunctionalInterface
  interface FourArgBlock<T1, T2, T3, T4>
      extends ParameterizedDefinitionBlock<FourArgBlock<T1, T2, T3, T4>> {
    void run(T1 arg0, T2 arg1, T3 arg2, T4 arg3);
  }

}
