package com.example.budgetbuddy;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Transaction {
    private String id;
    private String type; // "Expense" or "Income"
    private String category;
    private double amount;
    private String note;
    private String userId; // To filter by user
    private @ServerTimestamp Timestamp date; // Firestore timestamp

    // Empty constructor (required for Firestore)
    public Transaction() {}

    // Getters and setters
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
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}