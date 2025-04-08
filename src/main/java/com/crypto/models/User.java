package com.crypto.models;

import java.math.BigDecimal;

public class User {
    private int id;
    String name;
    private double balance = 10000.00;

    public User(){

    }

    public User(String name){
        this.name = name;
    }

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

