package com.greghaskins.spectrum;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class ConstructorBlock implements Block {

  private final Class<?> klass;
  private final Box<Object> box;

  ConstructorBlock(final Class<?> klass, Box<Object> box) {
    this.klass = klass;
    this.box = box;
  }

  @Override
  public void run() throws Throwable {
    try {
      final Constructor<?> constructor = this.klass.getDeclaredConstructor();
      constructor.setAccessible(true);
      box.set(constructor.newInstance());
    } catch (final InvocationTargetException invocationTargetException) {
      throw invocationTargetException.getTargetException();
    } catch (final Exception error) {
      throw new UnableToConstructSpecException(this.klass, error);
    }
  }

  private class UnableToConstructSpecException extends RuntimeException {

    private UnableToConstructSpecException(final Class<?> klass, final Throwable cause) {
      super(klass.getName(), cause);
    }

    private static final long serialVersionUID = 1L;

  }

}
