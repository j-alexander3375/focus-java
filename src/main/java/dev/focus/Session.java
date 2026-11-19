package dev.focus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A focus session: a fixed length of time starting at a known instant.
 *
 * <p>A session stores when it started and how long it lasts, never a running countdown, so its
 * state stays correct across process restarts, system sleep, and reads from other processes. All
 * time-dependent queries take a {@link Clock}, which lets tests use a fixed clock instead of the
 * real time.
 *
 * <p>Instances are immutable.
 *
 * @param start the instant the session began; never {@code null}
 * @param length how long the session lasts. Will always be positive
 * @param tag an optional label such as {@code "calculus"}, or {@code null} if untagged. Blank tags
 *     are normalized to {@code null}
 */
public record Session(Instant start, Duration length, String tag) {

  /**
   * Validates and normalizes the components.
   *
   * @throws NullPointerException if {@code start} or {@code length} is {@code null}
   * @throws IllegalArgumentException if {@code length} is zero or negative
   */
  public Session {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(length, "length");

    if (!length.isPositive()) {
      throw new IllegalArgumentException("Session length must be positive, got " + length);
    }

    if (tag != null && tag.isBlank()) {
      tag = null;
    }
  }

  /**
   * Returns the instant at which this session is scheduled to end.
   *
   * @return {@code start + length}
   */
  public Instant endsAt() {
    return start.plus(length);
  }

  /**
   * Returns the time left in this session at the clock's current instant.
   *
   * @param clock the source of the current instant
   * @return the time until {@link #endsAt()}, or {@link Duration#ZERO} if the session has finished.
   *     Can never be negative.
   */
  public Duration remaining(Clock clock) {
    Duration timeLeft = Duration.between(clock.instant(), endsAt());
    return timeLeft.isNegative() ? Duration.ZERO : timeLeft;
  }

  /**
   * Returns whether this session has finished at the clock's current instant.
   *
   * <p>A session is finished at exactly {@link #endsAt()}, not only after it.
   *
   * @param clock the source of the current instant
   * @return {@code true} if the current instant is at or after {@link #endsAt()}
   */
  public boolean isFinished(Clock clock) {
    return !clock.instant().isBefore(endsAt());
  }

  /**
   * Returns how much of this session has elapsed at the clock's current instant.
   *
   * <p>Result is clamped, it is {@link Duration#ZERO} if the clock is before {@link #start()}, and
   * {@link #length()} once the session has finished.
   *
   * @param clock the source of the current instant
   * @return the elapsed time, between zero and {@link #length()} inclusive
   */
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

  /**
   * Formats a duration for display as {@code MM:SS}, or {@code H:MM:SS} when it is an hour or
   * longer.
   *
   * <p>Examples: 25 minutes is {@code "25:00"}, 65 seconds is {@code "01:05"}, and 90 minutes is
   * {@code "1:30:00"}.
   *
   * @param d the duration to format; must not be negative
   * @return the formatted duration
   */
  public static String format(Duration d) {
    long hours = d.toHours();
    int minutes = d.toMinutesPart();
    int seconds = d.toSecondsPart();

    return hours > 0
        ? "%d:%02d:%02d".formatted(hours, minutes, seconds)
        : "%02d:%02d".formatted(minutes, seconds);
  }
}
