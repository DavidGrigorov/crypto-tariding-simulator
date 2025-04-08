package com.crypto.models;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private int userId;
    private String symbol;
    private String transactionType;
    private  double amount;
    private double price;
    private  double totalValue;
    private Timestamp timestamp;

    public Transaction(int id, int userId, String symbol, String transactionType,
                       double amount, double price, double totalValue, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.symbol = symbol;
        this.transactionType = transactionType;
        this.amount = amount;
        this.price = price;
        this.totalValue = totalValue;
        this.timestamp = timestamp;
    }

    public Transaction() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
