package com.charles.flashlight.torch

/**
 * Pure-Kotlin Morse code encoding (ITU) for the torch transmitter.
 *
 * [encode] returns a flat symbol string: '.' = dot, '-' = dash,
 * ' ' = letter gap, '/' = word gap. Unsupported characters are dropped.
 */
object MorseCode {

    private val MAP: Map<Char, String> = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--",
        '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.",
        '!' to "-.-.--", '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-",
        '&' to ".-...", ':' to "---...", ';' to "-.-.-.", '=' to "-...-",
        '+' to ".-.-.", '-' to "-....-", '_' to "..--.-", '"' to ".-..-.",
        '$' to "...-..-", '@' to ".--.-."
    )

    const val MAX_MESSAGE_LENGTH = 120

    /** True if [c] can be transmitted (letter, digit, supported punctuation, or space). */
    fun isSupported(c: Char): Boolean = c == ' ' || MAP.containsKey(c.uppercaseChar())

    /** Keep only transmittable characters, collapse repeated spaces, cap the length. */
    fun sanitize(text: String): String =
        text.filter { isSupported(it) }
            .replace(Regex("\\s+"), " ")
            .take(MAX_MESSAGE_LENGTH)

    /**
     * Encode [text] to the symbol string described above.
     * Example: encode("SOS") == "... --- ..."
     */
    fun encode(text: String): String {
        val clean = sanitize(text).trim()
        if (clean.isEmpty()) return ""
        return clean.split(' ').joinToString("/") { word ->
            word.mapNotNull { MAP[it.uppercaseChar()] }.joinToString(" ")
        }
    }
}
