package de.waldorfaugsburg.barista.mdb;

@FunctionalInterface
public interface MDBTransaction {
    boolean performTransaction(final MDBTransactionPayload data);
}
