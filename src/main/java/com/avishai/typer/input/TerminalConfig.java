package com.avishai.typer.input;

import java.io.IOException;

/*
The OS's terminal driver operates in "canonical mode" (line-buffered) by default.
It captures keystrokes and does not pass them to Java's standard input stream
until the user presses Enter.
Java has no native API to disable this.

===== IMPORTANT =====:
    I found a solution, but it won't work in windows without using external dependencies.
    It means this project will work on Unix only!

By default, the Unix terminal is icanon (canonical) mode.
To read raw keystrokes I must switch the terminal to non-canonical mode.

The Unix command to disable line buffering and pass characters immediately is:
    stty -icanon min 1
To restore it back to normal behavior before my program exits:
    stty icanon

BTY:
    I added -echo to the stty command to make sure "^C" won't print out when the user presses Ctrl+C
    It seems like the terminal emulator echos ^C when this keybinding triggers, and I don't want to see it.
    (When typing sudo and the terminal asks for the password, the terminal temporarily applies stty -echo
        So when the password is typed - nothing will appear on the screen)

Because Java's Runtime.getRuntime().exec() does not automatically run inside a TTY context,
I must rout the command through the shell and explicitly target the current terminal device (/dev/tty).

I'll also use a ProcessBuilder with .inheritIO
to make sure the subprocess gets the same redirectors and doesn't freeze

Once executed, System.in.read() will immediately return the integer byte value of the key pressed,
    without waiting for the Enter key.

I must register a shutdown hook to make sure the icanon mode is off before I exit.

--- A bit more about the stty command ---
stty: short for "Set Teletype", is the standard Unix utility used to modify the configuration
    parameters of the kernel-level terminal driver.

When I pass flags to stty - I'm directly mutating how the OS handles standard I/O for the current terminal session.
Placing the minus (-) sign in front of a flag in stty will make it disable that feature.
    -icanon instructs the terminal driver to disable canonical mode.

The 'min 1' parameter:
    In raw mode, the terminal driver relies on two variables to know when to send data to the application;
        * VMIN (minimum bytes)
        * VTIME (timeout)
    The command min 1 sets the VMIN variable to 1.
    This tells the kernel's read() to wait until 1 byte of data is available to instantly return it to the application.

The input redirection '< /dev/tty':
    When using Java's ProcessBuilder to spawn a new OS process,
    Java does not inherently attach that child process to the physical terminal window.
    Instead, Java creates anonymous background pipes to connect the child's I/O to the JVM.

    If I'll run 'stty -icanon min 1' the stty command will fail with the error
        'stty: standard input: Inappropriate ioctl for device'
    because it will see the Java memory pipe as it's standard input and realize it's not a real terminal.

    /dev/tty is a spacial device file that points at the current controlling terminal of the process accessing it.
    By using the shell redirection operator '< /dev/tty'
        I'm forcing the stty command to ignore Java's background memory pipes
        and read its configuration directly from the actual, physical Konsole window.

*/

public class TerminalConfig {
    private static volatile boolean isRawModeEnabled = false;

    public static void disableIcanon() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!isRawModeEnabled) return;
                new ProcessBuilder("sh", "-c", "stty icanon echo < /dev/tty")
                        .inheritIO()
                        .start()
                        .waitFor();

            } catch (IOException e) {
                System.err.print("Couldn't restore icanon mode: " + e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        try {
            int exitCode = new ProcessBuilder("sh", "-c", "stty -icanon -echo min 1 < /dev/tty")
                    .inheritIO()
                    .start()
                    .waitFor();

            if (exitCode == 0) {
                isRawModeEnabled = true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't disable icanon mode: ", e);
        }
    }
}
