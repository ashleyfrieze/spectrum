package com.greghaskins.spectrum;

import static com.greghaskins.spectrum.Spectrum.excludeTags;
import static com.greghaskins.spectrum.Spectrum.includeTags;
import static com.greghaskins.spectrum.SpectrumOptions.EXCLUDE_TAGS_PROPERTY;
import static com.greghaskins.spectrum.SpectrumOptions.INCLUDE_TAGS_PROPERTY;
import static com.greghaskins.spectrum.SpectrumOptions.TAGS_SEPARATOR;

import org.junit.runners.model.TestClass;

import java.util.Arrays;
import java.util.Optional;

/**
 * Decorator that applies tagging statements before calling decoratee.
 * Reads the tggging from the System properties and then the options.
 */
final class TaggingBlock implements Block {
  private final SpectrumOptions options;
  private final Block decoratee;

  TaggingBlock(final Class<?> klazz, final Block decoratee) {
    this.options = new TestClass(klazz).getAnnotation(SpectrumOptions.class);
    this.decoratee = decoratee;
  }

  @Override
  public void run() throws Throwable {
    final String[] systemIncludes = readSystemPropertyIncludes();
    final String[] systemExcludes = readSystemPropertyExcludes();

    final String[] annotationIncludes = readAnnotationIncludes();
    final String[] annotationExcludes = readAnnotationExcludes();

    // the annotation can provide nothing and so we drop back to
    // the system properties - this is done separately
    // for includes and excludes
    includeTags(firstNonBlank(annotationIncludes, systemIncludes));
    excludeTags(firstNonBlank(annotationExcludes, systemExcludes));

    // pass control to the decoratee
    decoratee.run();
  }

  private String[] firstNonBlank(final String[]... arrays) {
    return Arrays.stream(arrays)
        .filter(array -> array != null)
        .filter(array -> array.length > 0)
        .findFirst()
        .orElse(new String[] {});
  }

  private String[] fromSystemProperty(final String property) {
    return Optional.ofNullable(System.getProperty(property))
        .map(string -> string.split(TAGS_SEPARATOR))
        .filter(TaggingBlock::notArrayWithEmptyValue)
        .orElse(null);
  }

  private static boolean notArrayWithEmptyValue(final String[] array) {
    return !(array.length == 1 && array[0].isEmpty());
  }

  private String[] readSystemPropertyIncludes() {
    return fromSystemProperty(Optional.ofNullable(options)
        .map(SpectrumOptions::includeTagsSystemProperty)
        .orElse(INCLUDE_TAGS_PROPERTY));
  }

  private String[] readSystemPropertyExcludes() {
    return fromSystemProperty(Optional.ofNullable(options)
        .map(SpectrumOptions::excludeTagsSystemProperty)
        .orElse(EXCLUDE_TAGS_PROPERTY));
  }

  private String[] readAnnotationIncludes() {
    return Optional.ofNullable(options)
        .map(SpectrumOptions::includeTags)
        .orElse(null);
  }

  private String[] readAnnotationExcludes() {
    return Optional.ofNullable(options)
        .map(SpectrumOptions::excludeTags)
        .orElse(null);
  }

}
