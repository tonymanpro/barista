package de.waldorfaugsburg.barista.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class TextToSpeechUtil {

    public static void speak(final String soundFileName) {
        new Thread(() -> {
            try {
                final String command = "mpg123 tts/" + soundFileName + ".mp3";
                final String[] commandAndArgs = new String[]{"/bin/sh", "-c", command};
                final Process process = Runtime.getRuntime().exec(commandAndArgs);
                process.waitFor();
                final int exitValue = process.exitValue();
                if (exitValue != 0) {
                    log.error("TTS exit value was: " + exitValue);
                }
            } catch (final IOException | InterruptedException e) {
                log.error("An error occurred while running process", e);
            }
        }).start();
    }
}
