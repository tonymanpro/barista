package de.waldorfaugsburg.barista.ui;

import de.waldorfaugsburg.barista.BaristaApplication;
import de.waldorfaugsburg.barista.BaristaConfiguration;
import de.waldorfaugsburg.barista.util.TextToSpeechUtil;
import de.waldorfaugsburg.clerk.ChipReader;
import de.waldorfaugsburg.clerk.TransactionResponse;
import de.waldorfaugsburg.clerk.UserInformationResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public final class UserInterfaceService {

    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    public UserInterfaceService(final BaristaApplication application) {
        final String monitorPath = application.getConfiguration().getMdb().getMonitorPath();
        final ChipReader chipReader = new ChipReader(monitorPath);

        SERVICE.submit(() -> {
            while (!Thread.interrupted()) {
                // Blocking until chip is being read
                final String chipId = chipReader.awaitChip();

                final UserInformationResponse userInformation;
                if (chipId.length() != 10 || (userInformation = application.getClerk().getUserInformationByChip(chipId)) == null) {
                    TextToSpeechUtil.speak("INVALID_CHIP");
                    log.error("Invalid chip-id '{}' received!", chipId);
                    continue;
                }

                log.info("Chip-id '{}' identified as '{}'", chipId, userInformation.getUsername());
                application.getMdbService().startTransaction(payload -> {
                    final List<BaristaConfiguration.Product> products = application.getConfiguration().getProducts();

                    // Check if product id is out of bounds or not set
                    final BaristaConfiguration.Product product;
                    if (payload.getProductId() >= products.size()
                            || (product = products.get(payload.getProductId())) == null) {
                        TextToSpeechUtil.speak("INVALID_PRODUCT");
                        log.error("Invalid product-id '{}' received!", payload.getProductId());
                        return false;
                    }

                    log.info("Request for product '{}' ({}€) - ('{}') received!", payload.getProductId(), payload.getMoney(), product.getBarcode());

                    // Check for production grade restriction
                    if (product.getRestrictedGroups().contains(userInformation.getUserGroup())) {
                        TextToSpeechUtil.speak("RESTRICTED");
                        log.error("Product restricted for this user! Aborting...");
                        return false;
                    }

                    if (userInformation.getUsername().equals(application.getClerk().getUsername())) {
                        log.error("Service user! Skipping transaction...");
                        return true;
                    }

                    final TransactionResponse response = application.getClerk().performTransaction(chipId, product.getBarcode());
                    if (response != TransactionResponse.SUCCESS) {
                        TextToSpeechUtil.speak(response.name());
                        log.error("Transaction failed! ({})", response.name());
                        return false;
                    }

                    log.info("Transaction successful!");
                    return true;
                });
            }
        });
    }
}
