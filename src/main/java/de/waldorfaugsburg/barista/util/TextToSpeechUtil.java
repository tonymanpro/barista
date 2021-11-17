package de.waldorfaugsburg.barista.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class TextToSpeechUtil {

    public static void speak(final String text) {
        new Thread(() -> {
            try {
                final Process process = new ProcessBuilder(
                        "pico2wave", "-w", "tmp.wav",
                        "-l", "de-DE", "\"" + text + "\"",
                        "&&", "aplay", "tmp.wav").start();
                process.waitFor();
                final int exitValue = process.exitValue();
                if (exitValue != 0) {
                    log.error("TTS failed with exit code: {}", exitValue);
                }
            } catch (final IOException | InterruptedException e) {
                log.error("An error occurred while running process", e);
            }
        }).start();
    }
}
