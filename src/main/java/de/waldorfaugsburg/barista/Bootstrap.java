package de.waldorfaugsburg.barista;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.slf4j.bridge.SLF4JBridgeHandler;

@Slf4j
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void main(final String[] args) {
        final BaristaApplication application = new BaristaApplication();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            application.disable();
            LogManager.shutdown();
        }));

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        new Thread(application::enable).start();

        try {
            synchronized (application) {
                application.wait();
            }
        } catch (final InterruptedException e) {
            log.error("An error occurred while interrupting", e);
        }
    }
}
