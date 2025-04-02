package com.example.budgetbuddy;

public class Expense {
    private String id;
    private String title;
    private double amount;
    private String category;
    private String note;
    private String date;
    private String currency;

    public Expense() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}