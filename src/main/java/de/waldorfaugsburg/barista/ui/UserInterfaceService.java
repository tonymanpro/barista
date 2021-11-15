package de.waldorfaugsburg.barista.ui;

import de.waldorfaugsburg.barista.BaristaApplication;
import de.waldorfaugsburg.clerk.ChipCardReadingUtil;
import de.waldorfaugsburg.clerk.TransactionResponse;
import de.waldorfaugsburg.clerk.UserInformationResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class UserInterfaceService {

    public UserInterfaceService(final BaristaApplication application) {
        final String monitorPath = application.getProperties().getProperty("chip.monitorPath");
        new Thread(() -> {
            while (!Thread.interrupted()) {
                final String chipId = ChipCardReadingUtil.read(monitorPath);
                final UserInformationResponse userInformation = application.getClerk().getUserInformationByChip(chipId);
                if (userInformation == null) {
                    log.error("Invalid chip-id '{}' received!", chipId);
                    continue;
                }

                log.info("Chip-id '{}' identified as '{}'", chipId, userInformation.getUsername());
                application.getMdbService().startTransaction(payload -> {
                    final String barcode = application.getProperties().getProperty("barcode." + payload.getProductId());
                    if (barcode == null) {
                        log.error("Invalid product-id '{}' received!", payload.getProductId());
                        return false;
                    }

                    log.info("Transaction for product '{}' ({}€) - ('{}') received!", payload.getProductId(), payload.getMoney(), barcode);
                    final TransactionResponse response = application.getClerk().transaction(chipId, barcode);
                    if (response != TransactionResponse.SUCCESS) {
                        // TODO probably somehow show error to end-user
                        log.error("Transaction failed! ({})", response.name());
                        return false;
                    }

                    log.info("Transaction successful!");
                    return true;
                });
            }
        }).start();
    }

}
