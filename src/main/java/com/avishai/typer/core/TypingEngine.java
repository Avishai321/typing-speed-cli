package com.avishai.typer.core;

import com.avishai.typer.display.Renderer;
import com.avishai.typer.logic.TypingState;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TypingEngine {
    private final long statesUpdateNanos;
    private final ScheduledExecutorService scheduler;

    private final PromptEngine promptEngine;
    private final Renderer renderer;

    private final TypingState typingState;

    public TypingEngine(long statesUpdateNanos) {
        this.statesUpdateNanos = statesUpdateNanos;
        this.scheduler = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("states-update-thread");
            return thread;
        });

        this.promptEngine = new PromptEngine();
        this.renderer = new Renderer();

        this.typingState = new TypingState(this::onReset);
        this.typingState.setSystemPrompt(promptEngine.getNextPrompt());
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::preventStatesOverwrite));

        renderer.render(typingState); // First render

        this.scheduler.scheduleAtFixedRate(() -> {
            if (typingState.isTyping()) {
                renderer.render(typingState);
            }
        }, 0, this.statesUpdateNanos, TimeUnit.NANOSECONDS);

        while (true) {
            try {
                int input = System.in.read();
                typingState.processKey((char) input);

                if (typingState.isFinished()) {
                    break;
                }

                if (typingState.isCompleted()) {
                    typingState.setTextField("");
                    typingState.setSystemPrompt(promptEngine.getNextPrompt());
                }

                renderer.render(typingState);
            } catch (IOException e) {
                throw new RuntimeException("Input error during typing test:", e);
            }
        }

        preventStatesOverwrite();
        scheduler.shutdown();
    }

    private void onReset() {
        renderer.render(typingState);
    }

    private void preventStatesOverwrite() {
        System.out.print("\u001B[3E\r\n");
        System.out.flush();
    }
}
