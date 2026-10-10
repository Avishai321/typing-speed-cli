# Typing Speed CLI

A lightweight, zero-dependency command-line typing speed tester written in Java.

## OS Support
**Notice: Unix-Like Systems Only (Linux / macOS)**

I build this project to understand how to handle raw terminal input and dynamic ANSI rendering natively, 
without relying on heavy external native-binding libraries like JLine of JNA

Because Java's standard input is line-buffered by the OS by default,
this application uses ProcessBuilder to route stty commands directly to the /dev/tty device.
This toggles the terminal's canonical mode on and off safely, allowing for real-time keystroke interception.
Windows lacks these Unix device files and utilities, and supporting it would require injecting Win32 API wrappers.
I decided to keep this project clean, native, and dependency-free, so Windows is intentionally not supported.

## Features
* **Real-Time Input:** Bypasses canonical mode to process keys instantly without waiting for the Enter key.
* **Relative ANSI Rendering:** Uses relative cursor movements instead of absolute coordinates.
* **Thread-Safe State:** Handles blocking I/O and scheduled UI rendering loops concurrently without data races.
* **Live Metrics:** Calculates standard WPM, elapsed time (via high-resolution nanoTime), and accuracy on the fly.
* **Highlighting:** The system prompts background updates on the fly according to the user typed text.

## Interface Preview

```text
-------------------------------------------------
|                 System Prompt                 |
-------------------------------------------------
-------------------------------------------------
|                User Text Field                |
-------------------------------------------------
|Elapsed: 0.00s | WPM: 0.00 | Accuracy: 100.00% |
-------------------------------------------------
```

## Key Bindings
* **Ctrl+R:** Reset the current test and timer.
* **Ctrl+U:** Clear the entire user input field.
* **Ctrl+D:** Delete the last typed word.
* **Escape:** Exit the program.
