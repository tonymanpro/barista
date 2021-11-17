package de.waldorfaugsburg.barista.ui;

import de.waldorfaugsburg.barista.BaristaApplication;
import de.waldorfaugsburg.barista.util.TextToSpeechUtil;
import de.waldorfaugsburg.clerk.ChipCardReadingUtil;
import de.waldorfaugsburg.clerk.TransactionResponse;
import de.waldorfaugsburg.clerk.UserInformationResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

@Slf4j
public final class UserInterfaceService {

    public UserInterfaceService(final BaristaApplication application) {
        final String monitorPath = application.getProperties().getProperty("chip.monitorPath");
        new Thread(() -> {
            while (!Thread.interrupted()) {
                final String chipId = ChipCardReadingUtil.read(monitorPath);
                final UserInformationResponse userInformation = application.getClerk().getUserInformationByChip(chipId);
                if (userInformation == null) {
                    TextToSpeechUtil.speak("INVALID_CHIP");
                    log.error("Invalid chip-id '{}' received!", chipId);
                    continue;
                }

                log.info("Chip-id '{}' identified as '{}'", chipId, userInformation.getUsername());
                application.getMdbService().startTransaction(payload -> {
                    final String barcode = application.getProperties().getProperty("product." + payload.getProductId() + ".barcode");
                    if (barcode == null) {
                        log.error("Invalid product-id '{}' received!", payload.getProductId());
                        return false;
                    }

                    log.info("Request for product '{}' ({}€) - ('{}') received!", payload.getProductId(), payload.getMoney(), barcode);

                    // Check for production grade restriction
                    final Set<String> restrictedFor = readListFromProperty(application.getProperties()
                            .getProperty("product." + payload.getProductId() + ".restrictedFor"));

                    if (restrictedFor.contains(userInformation.getUserGroup())) {
                        TextToSpeechUtil.speak("RESTRICTED");
                        log.error("Product restricted for this user! Aborting...");
                        return false;
                    }

                    // Check if user is staff user
                    final Set<String> staffUsers = readListFromProperty(application.getProperties().getProperty("staff"));
                    if (staffUsers.contains(userInformation.getUsername())) {
                        log.info("Staff user! Skipping transaction...");
                        return true;
                    }

                    final TransactionResponse response = application.getClerk().transaction(chipId, barcode);
                    if (response != TransactionResponse.SUCCESS) {
                        TextToSpeechUtil.speak(response.name());
                        log.error("Transaction failed! ({})", response.name());
                        return false;
                    }

                    log.info("Transaction successful!");
                    return true;
                });
            }
        }).start();
    }

    private Set<String> readListFromProperty(final String property) {
        if (property == null) return Collections.emptySet();

        return Set.of(property.split(","));
    }
}
