package com.example.veb_app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String description;
    private String category;
    private double amount;
    private String type; // "INCOME" or "EXPENSE"
    private long transactionDate;
    private long createdDate;
    private long modifiedDate;
    
    public TransactionEntity(String description, String category, double amount, String type, long transactionDate) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate;
        this.createdDate = System.currentTimeMillis();
        this.modifiedDate = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { 
        this.amount = amount;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public String getType() { return type; }
    public void setType(String type) { 
        this.type = type;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public long getTransactionDate() { return transactionDate; }
    public void setTransactionDate(long transactionDate) { 
        this.transactionDate = transactionDate;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public long getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(long modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public Date getTransactionDateAsDate() { return new Date(transactionDate); }
    public Date getCreatedDateAsDate() { return new Date(createdDate); }
    public Date getModifiedDateAsDate() { return new Date(modifiedDate); }
}
