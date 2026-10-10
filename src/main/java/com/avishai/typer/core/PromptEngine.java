package com.avishai.typer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptEngine {
    private final List<String> currentCycle;
    private int currentIndex;

    public PromptEngine() {
        List<String> masterPrompts = List.of(
                "Java is a high-level, class-based, object-oriented programming language.",
                "Memory-mapped files allow direct OS-level buffer interactions.",
                "Containers provide isolated user environments and fast deployments.",
                "The Stream API enables functional-style operations on streams of elements.",
                "Records in Java provide a compact syntax for declaring immutable data carriers.",
                "The SOLID principles ensure software architectures remain maintainable and scalable.",
                "A ScheduledExecutorService can run concurrent tasks after a given delay or periodically.",
                "Garbage collection automatically reclaims heap memory occupied by unreachable objects.",
                "Volatile variables guarantee memory visibility across different execution threads.",
                "Wayland is a modern display server protocol intended to replace the legacy X11 system.",
                "KDE Plasma offers a highly customizable desktop environment for Unix-like operating systems.",
                "The stty command changes and prints terminal line settings on Unix systems.",
                "Canonical mode line buffering captures keystrokes until the enter key is pressed.",
                "A virtual machine runs an isolated guest operating system on top of a host hypervisor.",
                "Absolute path coordinates in a terminal will break when the window is resized.",
                "ANSI escape sequences control text formatting and cursor positions in a console.",
                "Docker Compose uses YAML files to configure and run multi-container applications.",
                "Data volumes persist application state independently of a container's lifecycle.",
                "Reverse proxies route incoming network requests to appropriate backend services.",
                "A mesh network creates a secure virtual private network between connected devices.",
                "Self-hosting requires careful management of user permissions and directory isolation.",
                "Continuous integration automatically builds and tests code changes in a shared repository.",
                "Synchronized blocks prevent thread interference and memory consistency errors.",
                "A shutdown hook allows a program to execute cleanup routines before it terminates.",
                "Extracting prompt generation into a dedicated engine follows the single responsibility principle.",
                "Polling a hardware device continuously consumes CPU cycles compared to interrupt-driven I/O."
        );

        this.currentCycle = new ArrayList<>(masterPrompts);
        this.currentIndex = 0;
        Collections.shuffle(this.currentCycle);
    }

    public String getNextPrompt() {
        if (currentIndex >= currentCycle.size()) {
            Collections.shuffle(currentCycle);
            currentIndex = 0;
        }
        return currentCycle.get(currentIndex++);
    }
}
