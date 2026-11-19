package dev.focus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * @param start  the beginning of a session.
 * @param length how long a session will last.
 * @param tag    what the session is used for. Can be null
 */
public record Session(Instant start, Duration length, String tag) {

  public Session {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(length, "length");

    if (!length.isPositive())
      throw new IllegalArgumentException("Duration of a Session can not be negative or 0.");

    if (tag != null && tag.isBlank())
      tag = null;
  }

  public Instant endsAt() {
    return start.plus(length);
  }

  public Duration remaining() {

  }

}
