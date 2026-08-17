package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "bills")
public class Bill {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String type;
    private double amount;
    private long timestamp;
    private String location;
    private String note;
    private String source;
    private String category;
    private long userId;
    private String currencySymbol = "\u00A5";
    private double originalAmount = 0;

    public Bill() {}

    @Ignore
    public Bill(String type, double amount, long timestamp, String location, String note, String source, String category, long userId) {
        this.type = type;
        this.amount = amount;
        this.originalAmount = amount;
        this.timestamp = timestamp;
        this.location = location;
        this.note = note;
        this.source = source;
        this.category = category;
        this.userId = userId;
    }

    @Ignore
    public Bill(String type, double amount, long timestamp, String location, String note, String source, String category, long userId, String currencySymbol) {
        this.type = type;
        this.amount = amount;
        this.originalAmount = amount;
        this.timestamp = timestamp;
        this.location = location;
        this.note = note;
        this.source = source;
        this.category = category;
        this.userId = userId;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "\u00A5";
    }

    @Ignore
    public Bill(String type, double amount, long timestamp, String location, String note, String source, String category, long userId, String currencySymbol, double originalAmount) {
        this.type = type;
        this.amount = amount;
        this.originalAmount = originalAmount;
        this.timestamp = timestamp;
        this.location = location;
        this.note = note;
        this.source = source;
        this.category = category;
        this.userId = userId;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "\u00A5";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getCurrencySymbol() { return currencySymbol != null ? currencySymbol : "\u00A5"; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol != null ? currencySymbol : "\u00A5"; }

    public double getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(double originalAmount) { this.originalAmount = originalAmount; }

    /**
     * Returns the display amount: originalAmount for foreign currency, amount for CNY.
     */
    public double getDisplayAmount() {
        String c = getCurrencySymbol();
        if (c == null || c.equals("\u00A5")) return amount;
        return originalAmount != 0 ? originalAmount : amount;
    }
}
