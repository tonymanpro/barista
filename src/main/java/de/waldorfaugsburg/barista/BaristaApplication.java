package de.waldorfaugsburg.barista;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.barista.mdb.MDBService;
import de.waldorfaugsburg.barista.ui.UserInterfaceService;
import de.waldorfaugsburg.clerk.Clerk;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;

@Slf4j
public final class BaristaApplication {

    private BaristaConfiguration configuration;
    private Gson gson;

    private Clerk clerk;
    private MDBService mdbService;
    private UserInterfaceService userInterfaceService;

    public BaristaApplication() {

    }

    public void enable() {
        gson = new GsonBuilder().create();
        try (final JsonReader reader = new JsonReader(new FileReader("config.json"))) {
            configuration = gson.fromJson(reader, BaristaConfiguration.class);
        } catch (final IOException e) {
            log.error("An error occurred while reading configuration", e);
            System.exit(1);
        }

        log.info("{} products registered!", configuration.getProducts().size());

        clerk = new Clerk(
                configuration.getClerk().getDriver(),
                configuration.getClerk().getProjectId(),
                configuration.getClerk().getFacilityId(),
                configuration.getClerk().getUsername(),
                configuration.getClerk().getPassword());
        mdbService = new MDBService(this);
        userInterfaceService = new UserInterfaceService(this);
    }

    public void disable() {

    }

    public BaristaConfiguration getConfiguration() {
        return configuration;
    }

    public Gson getGson() {
        return gson;
    }

    public Clerk getClerk() {
        return clerk;
    }

    public MDBService getMdbService() {
        return mdbService;
    }

    public UserInterfaceService getUserInterfaceService() {
        return userInterfaceService;
    }
}
