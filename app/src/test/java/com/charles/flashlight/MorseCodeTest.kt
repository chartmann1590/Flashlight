package com.charles.flashlight

import com.charles.flashlight.torch.MorseCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MorseCodeTest {

    @Test
    fun sosEncodesCorrectly() {
        assertEquals("... --- ...", MorseCode.encode("SOS"))
    }

    @Test
    fun lowercaseAndWordGaps() {
        assertEquals("... --- .../... --- ...", MorseCode.encode("sos sos"))
    }

    @Test
    fun unsupportedCharactersAreDropped() {
        assertEquals(".... ..", MorseCode.encode("HI~"))
        assertEquals("", MorseCode.encode("~~~"))
        assertEquals("", MorseCode.encode(""))
    }

    @Test
    fun sanitizeCapsLengthAndCollapsesSpaces() {
        val cleaned = MorseCode.sanitize("  hello    world  ")
        assertEquals("hello world", cleaned.trim())
        assertTrue(MorseCode.sanitize("a".repeat(500)).length <= MorseCode.MAX_MESSAGE_LENGTH)
    }
}
