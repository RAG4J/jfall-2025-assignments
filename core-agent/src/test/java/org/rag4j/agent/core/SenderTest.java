package org.rag4j.agent.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenderTest {

    @Test
    void returnsCorrectDisplayNameForEachEnumConstant() {
        assertEquals("System", Sender.SYSTEM.displayName());
        assertEquals("User", Sender.USER.displayName());
        assertEquals("Assistant", Sender.ASSISTANT.displayName());
        assertEquals("Observation", Sender.OBSERVATION.displayName());
    }

    @Test
    void toStringReturnsDisplayName() {
        assertEquals("System", Sender.SYSTEM.toString());
        assertEquals("User", Sender.USER.toString());
        assertEquals("Assistant", Sender.ASSISTANT.toString());
        assertEquals("Observation", Sender.OBSERVATION.toString());
    }

    @Test
    void fromReturnsCorrectEnumForExactDisplayName() {
        assertEquals(Sender.SYSTEM, Sender.from("System"));
        assertEquals(Sender.USER, Sender.from("User"));
        assertEquals(Sender.ASSISTANT, Sender.from("Assistant"));
        assertEquals(Sender.OBSERVATION, Sender.from("Observation"));
    }

    @Test
    void fromIsCaseInsensitive() {
        assertEquals(Sender.SYSTEM, Sender.from("system"));
        assertEquals(Sender.USER, Sender.from("USER"));
        assertEquals(Sender.ASSISTANT, Sender.from("assistant"));
        assertEquals(Sender.OBSERVATION, Sender.from("observation"));
    }

    @Test
    void fromThrowsExceptionForUnknownDisplayName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Sender.from("Unknown"));
        assertTrue(ex.getMessage().contains("Unknown sender"));
    }

    @Test
    void fromThrowsExceptionForNullDisplayName() {
        assertThrows(IllegalArgumentException.class, () -> Sender.from(null));
    }
}