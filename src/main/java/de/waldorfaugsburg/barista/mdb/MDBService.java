package de.waldorfaugsburg.barista.mdb;

import com.pi4j.io.serial.*;
import de.waldorfaugsburg.barista.BaristaApplication;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
/**
 * Qibixx MDB service
 * @see https://docs.qibixx.com/mdb-products/mdb-products
 */
public final class MDBService {

    private final int arbitraryStartMoney;
    private final int selectionTimeoutMillis;

    private Serial serial;
    private MDBTransaction transaction;

    public MDBService(final BaristaApplication application) {
        this.arbitraryStartMoney = Integer.parseInt(application.getProperties().getProperty("mdb.arbitraryStartMoney"));
        this.selectionTimeoutMillis = Integer.parseInt(application.getProperties().getProperty("mdb.selectionTimeoutMillis"));

        try {
            serial = SerialFactory.createInstance();
            // Opening serial connection
            serial.open(new SerialConfig()
                    .device(SerialPort.getDefaultPort())
                    .baud(Baud._115200)
                    .dataBits(DataBits._8)
                    .parity(Parity.NONE)
                    .stopBits(StopBits._1));
            log.info("OPENED");

            serial.addListener(event -> {
                try {
                    final String data = event.getAsciiString();
                    handleIncomingData(data);
                    log.info("Incoming data " + data);
                } catch (final IOException e) {
                    log.info("An error occurred while handling data", e);
                }
            });

            // Sending version command
            send("V");

            // Enabling "cashless slave" mode
            send("C", "1");
        } catch (final IOException | InterruptedException e) {
            log.error("An error occurred while initializing device", e);
        }
    }

    public void startTransaction(final MDBTransaction transaction) {
        this.transaction = transaction;

        // Sending arbitrary start money to initiate vending session
        send("C", "START", Integer.toString(arbitraryStartMoney));

        // Defining timestamp of loop start; in order to calculate time elapsed since
        final long startMillis = System.currentTimeMillis();

        // Waiting for transacting to be fulfilled or timeout
        while (transaction != null) {
            if (System.currentTimeMillis() - startMillis >= selectionTimeoutMillis) {
                // Sending vending stop command if timeout reached
                send("C", "STOP");
                // TODO probably somehow show error to end-user
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                log.error("An error occurred while interrupting", e);
            }
        }
    }

    private void handleIncomingData(final String data) {
        final String[] parsedData = parseData(data);

        // Reading incoming data
        if (parsedData[0].equals("c") && parsedData[1].equals("STATUS") && parsedData[2].equals("VEND")) {
            final double money = Double.parseDouble(parsedData[3]);
            final int productId = Integer.parseInt(parsedData[4]);

            final boolean success = transaction.performTransaction(new MDBTransactionPayload(money, productId));
            send("C", "VEND", success ? Double.toString(money) : "-1");

            // Resetting transaction in order to leave loop above
            transaction = null;
        } else if (parsedData[0].equals("v")) {
            log.info("Using MDB version: " + parsedData[1]);
        }
    }

    private void send(final String... args) {
        if (serial.isClosed())
            return;

        try {
            serial.writeln(String.join(",", args));
        } catch (final IOException e) {
            log.error("An error occurred while sending data", e);
        }
    }

    private String[] parseData(final String data) {
        return data.split(",");
    }
}
