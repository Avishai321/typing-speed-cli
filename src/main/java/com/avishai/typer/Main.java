package com.avishai.typer;

import com.avishai.typer.core.TypingEngine;
import com.avishai.typer.input.TerminalConfig;

public class Main {
    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.err.println("Warning: Raw terminal mode requires a Unix-like environment.");
            return;
        }

        TerminalConfig.disableIcanon();
        new TypingEngine(100_000_000).start();
    }
}
