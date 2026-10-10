package com.avishai.typer;

public class WpmCalculator {
    public static double calculateWPM(int totalChars, long startNanos, long endNanos) {
        double elapsedNanos = endNanos - startNanos;
        double elapsedMinutes = elapsedNanos / 1_000_000_000.0 / 60.0;
        double standardWords = totalChars / 5.0;

        return standardWords / elapsedMinutes;
    }
}
