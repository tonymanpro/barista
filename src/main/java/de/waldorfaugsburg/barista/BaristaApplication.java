package de.waldorfaugsburg.barista;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.waldorfaugsburg.barista.mdb.MDBService;
import de.waldorfaugsburg.barista.ui.UserInterfaceService;
import de.waldorfaugsburg.clerk.Clerk;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public final class BaristaApplication {

    private Properties properties;
    private Gson gson;

    private Clerk clerk;
    private MDBService mdbService;
    private UserInterfaceService userInterfaceService;

    public BaristaApplication() {

    }

    public void enable() {
        properties = new Properties();
        try {
            properties.load(new BufferedReader(new FileReader("config.properties")));
        } catch (final IOException e) {
            log.error("An error occurred while reading properties", e);
            System.exit(1);
        }

        gson = new GsonBuilder().create();
        clerk = new Clerk(
                properties.getProperty("clerk.driver"),
                properties.getProperty("clerk.projectId"),
                properties.getProperty("clerk.facilityId"),
                properties.getProperty("clerk.username"),
                properties.getProperty("clerk.password"));
        mdbService = new MDBService(this);
        userInterfaceService = new UserInterfaceService(this);
    }

    public void disable() {

    }

    public Properties getProperties() {
        return properties;
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
