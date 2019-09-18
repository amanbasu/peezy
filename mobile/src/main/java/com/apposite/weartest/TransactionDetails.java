package com.apposite.weartest;

class TransactionDetails {

    private double amount;
    private String currency, person;

    public TransactionDetails(double amount, String currency, String person) {
        this.amount = amount;
        this.currency = currency;
        this.person = person;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPerson() {
        return person;
    }
}
