package com.avishai.typer.logic;

/*
--- TIME STATE MANAGEMENT & WPM CALCULATION ---
The WPM Formula:
    In professional typing tests, a "word" is not defined by spaces or actual dictionary words.
    A "standard word" is strictly defined as exactly 5 characters, including spaces and punctuation.
    Therefore, if a user types 25 characters, they have typed exactly 5 standard words,
    regardless of how many spaces were used.

The formula is:
WPM = (Total Characters / 5) / Elapsed Time in Minutes
 */
public class WpmCalculator {
    public static double calculateWPM(int totalChars, double elapsedNanos) {
        double elapsedMinutes = elapsedNanos / 1_000_000_000.0 / 60.0;
        double standardWords = totalChars / 5.0;

        return standardWords / elapsedMinutes;
    }
}
