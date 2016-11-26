package com.greghaskins.spectrum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Options that can be applied to a test class annotated with {@link org.junit.runner.RunWith}
 * with the {@link Spectrum} runner. E.g.<br>
 * <pre><code class="java">
 *     &#064;RunWith(Spectrum.class)
 *     &#064;SpectrumOptions(includeTags={"wip","dev"})
 *     public class MyTest { ... }
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SpectrumOptions {
  String INCLUDE_TAGS_PROPERTY = "spectrum.include.tags";
  String EXCLUDE_TAGS_PROPERTY = "spectrum.exclude.tags";
  String TAGS_SEPARATOR = ",";

  /**
   * Allows tags to be selected for controlling the test at coding time.
   * This means you can supply tags which relate to, say, Work In Progress
   * specs temporarily while developing them. See {@link Spectrum#tag(String...)}
   * @return the tags hard coded for inclusion.
   */
  String[] includeTags() default {};

  /**
   * Allows tags to be selected for controlling the test at coding time.
   * This means you can supply tags while developing. See {@link Spectrum#tag(String...)}
   * @return the tags hard coded for exclusion.
   */
  String[] excludeTags() default {};

  /**
   * Which system property can be used to retrieve tags passed in by
   * property. These tags are to be comma separated in the property.
   * See {@link Spectrum#tag(String...)}
   * @return the system property to use if not default.
   */
  String includeTagsSystemProperty() default INCLUDE_TAGS_PROPERTY;

  /**
   * Which system property can be used to retrieve tags passed in by
   * property. These tags are to be comma separated in the property.
   * See {@link Spectrum#tag(String...)}
   * @return the system property to use if not default.
   */
  String excludeTagsSystemProperty() default EXCLUDE_TAGS_PROPERTY;
}
