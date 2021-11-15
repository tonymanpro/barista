package de.waldorfaugsburg.barista.mdb;

public final class MDBTransactionPayload {

    private final double money;
    private final int productId;

    public MDBTransactionPayload(final double money, final int productId) {
        this.money = money;
        this.productId = productId;
    }

    public double getMoney() {
        return money;
    }

    public int getProductId() {
        return productId;
    }
}
