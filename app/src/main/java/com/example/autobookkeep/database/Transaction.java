package com.example.autobookkeep.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions",
        indices = {
                @Index("date"),
                @Index("type"),
                @Index("category")
        })
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long date;
    public int type;        // 0=支出, 1=收入
    public String category;
    public String subCategory;
    public double amount;
    public String note;
    public String remark;
    public int assetId;
    public String currencySymbol;
    public String photoPath;
    public String targetObject;
    public boolean excludeFromBudget;

    public Transaction() {
        this.currencySymbol = "¥";
        this.subCategory = "";
        this.photoPath = "";
        this.excludeFromBudget = false;
    }
}