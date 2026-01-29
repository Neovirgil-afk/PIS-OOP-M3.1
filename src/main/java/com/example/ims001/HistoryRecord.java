package com.example.ims001;

public class HistoryRecord {
    private final int id;
    private final String action;
    private final String productName;
    private final String details;
    private final String createdAt;

    public HistoryRecord(int id, String action, String productName, String details, String createdAt) {
        this.id = id;
        this.action = action;
        this.productName = productName;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getAction() { return action; }
    public String getProductName() { return productName; }
    public String getDetails() { return details; }
    public String getCreatedAt() { return createdAt; }
}
