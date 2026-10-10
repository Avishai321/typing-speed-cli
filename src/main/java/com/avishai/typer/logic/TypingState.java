package com.avishai.typer.logic;

public class TypingState implements SnapshotProvider {
    enum State {
        PENDING,
        TYPING,
        FINISHED
    }

    public record TextSnapshot(
            String systemPrompt,
            String typedText) {
    }

    public record StatesSnapshot(
            double elapsedNanos,
            double wpm,
            double accuracy) {
    }

    private String systemPrompt;
    private final StringBuilder textField = new StringBuilder();
    private int lengthTyped = 0;

    private State state = State.PENDING;

    private long startNano = -1;
    private long endNano = -1;

    private final Runnable onReset;

    public TypingState(Runnable onReset) {
        this.onReset = onReset;
    }

    public synchronized void processKey(char c) {
        // Escape / Ctrl+C (Exit)
        if (c == '\u001B' || c == '\u0003') {
            setFinished();
            return;
        }

        // Backspace & Delete (delete the last character).
        if (c == '\b' || c == '\u007F') {
            if (textField.isEmpty()) return;

            textField.setLength(textField.length() - 1);
            lengthTyped--;
            return;
        }

        // Ctrl+D (Delete last word)
        if (c == '\u0004') {
            int lastSpaceIndex = Math.max(textField.lastIndexOf(" "), 0);

            lengthTyped -= textField.length() - lastSpaceIndex;
            textField.replace(lastSpaceIndex, textField.length(), "");
            return;
        }

        // Ctrl+U (rest text field).
        if (c == '\u0015') {
            lengthTyped -= textField.length();
            setTextField("");
            return;
        }

        // Ctrl+R (reset states and text field).
        if (c == '\u0012') {
            startNano = -1;
            endNano = -1;
            lengthTyped = 0;
            state = State.PENDING;
            setTextField("");

            if (this.onReset != null) {
                onReset.run();
            }
            return;
        }

        if (this.startNano == -1) {
            this.startNano = System.nanoTime();
        }
        textField.append(c);
        lengthTyped++;

        state = State.TYPING;
    }

    public synchronized boolean isCompleted() {
        return getTypedText().length() == getSystemPrompt().length()
                && getAccuracy() == 100.0;
    }

    public synchronized boolean isFinished() {
        return this.state.equals(State.FINISHED);
    }

    public synchronized double getAccuracy() {
        if (textField.isEmpty()) return 100.0;

        int correctChars = 0;
        int limit = Math.min(textField.length(), systemPrompt.length());

        for (int i = 0; i < limit; i++) {
            if (textField.charAt(i) == systemPrompt.charAt(i)) {
                correctChars++;
            }
        }

        return ((double) correctChars / textField.length()) * 100;
    }

    public synchronized double getWpm() {
        if (this.state.equals(State.PENDING)) return 0.0;

        return WpmCalculator.calculateWPM(
                getLengthTyped(),
                getElapsedNanos()
        );
    }

    public synchronized double getElapsedNanos() {
        if (this.startNano == -1) return 0.0;

        if (this.state.equals(State.FINISHED)) {
            return this.endNano - this.startNano;
        } else {
            return System.nanoTime() - this.startNano;
        }
    }

    public synchronized String getSystemPrompt() {
        if (this.systemPrompt == null) return "";
        return this.systemPrompt;
    }

    public synchronized void setSystemPrompt(String prompt) {
        this.systemPrompt = prompt;
    }

    public synchronized String getTypedText() {
        return this.textField.toString();
    }

    public synchronized int getLengthTyped() {
        return this.lengthTyped;
    }

    public synchronized void setFinished() {
        this.endNano = System.nanoTime();
        this.state = State.FINISHED;
    }

    public synchronized boolean isTyping() {
        return this.state.equals(State.TYPING);
    }

    public synchronized TextSnapshot getTextSnapshot() {
        return new TextSnapshot(
                getSystemPrompt(),
                getTypedText()
        );
    }

    public synchronized StatesSnapshot getStatesSnapshot() {
        return new StatesSnapshot(
                getElapsedNanos(),
                getWpm(),
                getAccuracy()
        );
    }

    public synchronized void setTextField(String text) {
        this.textField.setLength(0);
        this.textField.append(text);
    }
}
