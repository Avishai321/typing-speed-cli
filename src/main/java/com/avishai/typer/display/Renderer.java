package com.avishai.typer.display;

import com.avishai.typer.logic.SnapshotProvider;
import lombok.Setter;

import static com.avishai.typer.logic.TypingState.StatesSnapshot;
import static com.avishai.typer.logic.TypingState.TextSnapshot;

/*
In a standard console application, using System.out.println() appends text to a new line
and scrolls the terminal down.
For a typing speed CLI, I must keep the text static on the screen and update the user's input,
colors, and cursor position in place.

To achieve this, I should use ANSI Escape Codes - spacial string sequences intercepted by the terminal emulator
(like Konsole) to control formatting and cursor behavior rather than printing literal characters.

An ANSI sequence begins with the Escape character.
In Java, this is represented using the Unicode escape \u001B.
Following the Escape character is usually the Control Sequence Introducer (CSI), which is a bracket [.

To overwrite a line in place, I need two operations:
1. Move the cursor to the start of the line: The standard Carriage Return character \r
2. Clear the existing text: If I print a short word over a long word,
    the trailing characters of the long word will still be visible.
    The ANSI sequence to clear the entire current line is \u001B[2K

By combining these, printing \r\u001B[2K instantly resets the cursor to the first column
and wipes the line clean, ready for the new string.
 */
@Setter
public class Renderer {
    private final StringBuilder sb = new StringBuilder();

    public synchronized void render(SnapshotProvider snapshotProvider) {
        TextSnapshot textSnapshot = snapshotProvider.getTextSnapshot();
        StatesSnapshot statesSnapshot = snapshotProvider.getStatesSnapshot();

        sb.setLength(0);
        sb.append(clear()).append(resetCursor());

        // System prompt.
        appendSysPrompt(textSnapshot);

        // User text field.
        sb.append("\n").append(textSnapshot.typedText());

        // States.
        sb.append("\n\n")
                .append("Elapsed: ")
                .append(String.format("%.2f", statesSnapshot.elapsedNanos() / 1_000_000_000.0))
                .append("s | ")
                .append("WPM: ")
                .append(String.format("%.2f", statesSnapshot.wpm()))
                .append(" | ")
                .append("Accuracy: ")
                .append(String.format("%.2f", statesSnapshot.accuracy()))
                .append("%");

        // Move the cursor back UP from the stats line to the user text field.
        sb.append(moveToUserTextField())
                .append("\r")
                .append(moveRight(textSnapshot.typedText().length()));

        System.out.print(sb);
        System.out.flush();
    }

    private void appendSysPrompt(TextSnapshot textSnapshot) {
        String systemPrompt = textSnapshot.systemPrompt();
        String userText = textSnapshot.typedText();

        // Coloring a char is done like that for char c:
        //  "\u001B[" + color (42 for green, 41 for red) + "m" + c
        int limit = Math.clamp(userText.length(), 0, systemPrompt.length());
        for (int i = 0; i < limit; i++) {
            sb.append("\u001B[");

            sb.append((systemPrompt.charAt(i) == userText.charAt(i)
                    ? "42" // Green background
                    : "41" // Red background
            ));

            sb.append("m").append(systemPrompt.charAt(i));
        }

        sb.append("\u001B[0m"); // Reset colors to default.
        sb.append(systemPrompt.substring(limit));
    }

    private String clear() {
        return "\u001B[2J";
    }

    private String resetCursor() {
        return "\u001B[H";
    }

    private String moveToUserTextField() {
        return "\u001B[" + 2 + "A";
    }

    private String moveRight(int steps) {
        if (steps == 0) return ""; // Prevents cursor jumps
        return "\u001B[" + steps + "C";
    }
}
