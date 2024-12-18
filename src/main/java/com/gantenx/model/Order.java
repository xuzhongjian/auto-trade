package com.gantenx.model;

public class Order extends Time {
    private final String type;       // "buy" or "sell"
    private final double price;      // 价格
    private final double quantity;   // 数量
    private final String symbol;    // 标的符号

    public Order(String symbol, String type, double price, double quantity, long timestamp) {
        super(timestamp);
        this.symbol = symbol;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getSymbol() {
        return this.symbol;
    }


    @Override
    public String toString() {
        return "Order{" +
                "type='" + type + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + super.getTimestamp() +
                '}';
    }
}
