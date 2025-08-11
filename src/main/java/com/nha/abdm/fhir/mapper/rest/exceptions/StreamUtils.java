/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.exceptions;

import java.util.function.Function;

public class StreamUtils {

  @FunctionalInterface
  public interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
  }

  public static <T, R> Function<T, R> wrapException(CheckedFunction<T, R> function) {
    return t -> {
      try {
        return function.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(
            "Exception in stream processing"
                + e.getMessage()
                + " "
                + e.getCause()
                + " "
                + e.getLocalizedMessage(),
            e);
      }
    };
  }
}
