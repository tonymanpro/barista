package de.waldorfaugsburg.barista;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
public final class BaristaConfiguration {

    private ClerkConfiguration clerk;
    private MDBConfiguration mdb;
    private List<Product> products;

    @NoArgsConstructor
    @Getter
    public static final class ClerkConfiguration {
        private String driver;
        private String projectId;
        private String facilityId;
        private String username;
        private String password;
    }

    @NoArgsConstructor
    @Getter
    public static final class MDBConfiguration {
        private String monitorPath;
        private int arbitraryStartMoney;
        private long selectionTimeoutMillis;
    }

    @NoArgsConstructor
    @Getter
    public static final class Product {
        private String barcode;
        private Set<String> restrictedGroups;
    }
}
