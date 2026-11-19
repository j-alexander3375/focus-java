package dev.focus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * @param start the beginning of a session.
 * @param length how long a session will last.
 * @param tag what the session is used for. Can be null
 */
public record Session(Instant start, Duration length, String tag) {

  public Session {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(length, "length");

    if (!length.isPositive())
      throw new IllegalArgumentException("Duration of a Session can not be negative or 0.");

    if (tag != null && tag.isBlank()) tag = null;
  }

  public Instant endsAt() {
    return start.plus(length);
  }

  public Duration remaining(Clock clock) {
    Duration timeLeft = Duration.between(clock.instant(), endsAt());
    return timeLeft.isNegative() ? Duration.ZERO : timeLeft;
  }

  public boolean isFinished(Clock clock) {
    Instant now = clock.instant();
    return now.isAfter(endsAt()) || now.equals(endsAt());
  }

  public Duration elapsed(Clock clock) {
    Duration timeElapsed = Duration.between(start, clock.instant());

    if (timeElapsed.isNegative()) {
      return Duration.ZERO;
    }

    if (timeElapsed.compareTo(length) > 0) {
      return length;
    }

    return timeElapsed;
  }

  public static String format(Duration d) {
    long hours = d.toHours();
    int minutes = d.toMinutesPart();
    int seconds = d.toSecondsPart();

    return hours > 0
        ? "%d:%02d:%02d".formatted(hours, minutes, seconds)
        : "%02d:%02d".formatted(minutes, seconds);
  }
}
