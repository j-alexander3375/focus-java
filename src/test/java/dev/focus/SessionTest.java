package dev.focus;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SessionTest {

  private static final Instant START = Instant.parse("2026-11-19T10:00:00Z");
  private final Session session = new Session(START, Duration.ofMinutes(25), "german");

  /** A clock frozen at START + offset, so tests never depend on the real time. */
  private static Clock at(Duration offset) {
    return Clock.fixed(START.plus(offset), ZoneOffset.UTC);
  }

  @Test
  void rejectsNonPositiveLength() {
    assertThrows(IllegalArgumentException.class, () -> new Session(START, Duration.ZERO, null));
    assertThrows(
        IllegalArgumentException.class, () -> new Session(START, Duration.ofMinutes(-5), null));
  }

  @Test
  void rejectsNulls() {
    assertThrows(NullPointerException.class, () -> new Session(null, Duration.ofMinutes(25), null));
    assertThrows(NullPointerException.class, () -> new Session(START, null, null));
  }

  @Test
  void tagIsOptional() {
    assertDoesNotThrow(() -> new Session(START, Duration.ofMinutes(25), null));
  }

  @Test
  void endsAtIsStartPlusLength() {
    assertEquals(Instant.parse("2026-11-19T10:25:00Z"), session.endsAt());
  }

  @Test
  void remainingCountsDown() {
    assertEquals(Duration.ofMinutes(25), session.remaining(at(Duration.ZERO)));
    assertEquals(Duration.ofMinutes(15), session.remaining(at(Duration.ofMinutes(10))));
  }

  @Test
  void remainingNeverGoesNegative() {
    assertEquals(Duration.ZERO, session.remaining(at(Duration.ofMinutes(40))));
  }

  @Test
  void finishedExactlyAtTheEnd() {
    assertFalse(session.isFinished(at(Duration.ofMinutes(25).minusSeconds(1))));
    assertTrue(session.isFinished(at(Duration.ofMinutes(25))));
    assertTrue(session.isFinished(at(Duration.ofHours(3))));
  }

  @Test
  void elapsedIsClamped() {
    assertEquals(Duration.ofMinutes(10), session.elapsed(at(Duration.ofMinutes(10))));
    assertEquals(Duration.ofMinutes(25), session.elapsed(at(Duration.ofHours(1))));
    assertEquals(Duration.ZERO, session.elapsed(at(Duration.ofMinutes(-5))));
  }

  @Test
  void formatsMinutesAndSeconds() {
    assertEquals("25:00", Session.format(Duration.ofMinutes(25)));
    assertEquals("01:05", Session.format(Duration.ofSeconds(65)));
    assertEquals("00:00", Session.format(Duration.ZERO));
  }

  @Test
  void formatsHoursWhenNeeded() {
    assertEquals("1:30:00", Session.format(Duration.ofMinutes(90)));
    assertEquals("2:00:05", Session.format(Duration.ofHours(2).plusSeconds(5)));
  }
}
