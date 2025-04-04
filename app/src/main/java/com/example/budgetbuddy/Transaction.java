package com.example.budgetbuddy;

import com.google.firebase.Timestamp;

public class Transaction {
    private String id;
    private String type;
    private String category;
    private double amount;
    private String note;
    private Timestamp timestamp; // Firestore Timestamp
    private String userId;
    private String currency;

    // Default constructor (required for Firestore)
    public Transaction() {}

    // Parameterized constructor
    public Transaction(String id, String type, String category, double amount, String note, Timestamp timestamp, String userId, String currency) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.timestamp = timestamp;
        this.userId = userId;
        this.currency = currency;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}