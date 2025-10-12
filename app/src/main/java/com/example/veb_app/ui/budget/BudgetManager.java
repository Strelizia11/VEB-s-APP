package com.example.veb_app.ui.budget;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BudgetManager {
    private static BudgetManager instance;
    private final List<Transaction> transactions;
    private final List<BudgetCategory> incomeCategories;
    private final List<BudgetCategory> expenseCategories;
    private double monthlyBudget;
    private SharedPreferences prefs;

    private BudgetManager() {
        transactions = new ArrayList<>();
        incomeCategories = new ArrayList<>();
        expenseCategories = new ArrayList<>();
        monthlyBudget = 0.0; // Default monthly budget - user will set their own
        initializeDefaultCategories();
    }

    public static synchronized BudgetManager getInstance() {
        if (instance == null) {
            instance = new BudgetManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE);
        loadData();
    }

    private void initializeDefaultCategories() {
        // Income Categories - Only 4 specific categories
        incomeCategories.add(new BudgetCategory("From Mama", "#4CAF50", 0.0));
        incomeCategories.add(new BudgetCategory("From Papa", "#8BC34A", 0.0));
        incomeCategories.add(new BudgetCategory("From Work", "#2E7D32", 0.0));
        incomeCategories.add(new BudgetCategory("Others", "#388E3C", 0.0));
        
        // Expense Categories - Separate from income categories
        expenseCategories.add(new BudgetCategory("Food & Dining", "#FF6B6B", 500.0));
        expenseCategories.add(new BudgetCategory("Transportation", "#4ECDC4", 300.0));
        expenseCategories.add(new BudgetCategory("Entertainment", "#45B7D1", 200.0));
        expenseCategories.add(new BudgetCategory("Bills & Utilities", "#96CEB4", 400.0));
        expenseCategories.add(new BudgetCategory("Shopping", "#FECA57", 300.0));
        expenseCategories.add(new BudgetCategory("Healthcare", "#FF9FF3", 200.0));
        expenseCategories.add(new BudgetCategory("Education", "#54A0FF", 300.0));
        expenseCategories.add(new BudgetCategory("Other Expenses", "#5F27CD", 300.0));
    }

    // Transaction Management
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        saveData();
    }

    public void updateTransaction(Transaction updatedTransaction) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId() == updatedTransaction.getId()) {
                transactions.set(i, updatedTransaction);
                break;
            }
        }
        saveData();
    }

    public void deleteTransaction(Transaction transaction) {
        transactions.remove(transaction);
        saveData();
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<Transaction> getTransactionsByCategory(String category) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCategory().equals(category)) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    public List<Transaction> getTransactionsByMonth(int year, int month) {
        List<Transaction> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (Transaction transaction : transactions) {
            cal.setTime(transaction.getDate());
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    // Category Management
    public List<BudgetCategory> getAllCategories() {
        List<BudgetCategory> allCategories = new ArrayList<>();
        allCategories.addAll(incomeCategories);
        allCategories.addAll(expenseCategories);
        return allCategories;
    }
    
    public List<BudgetCategory> getIncomeCategories() {
        return new ArrayList<>(incomeCategories);
    }
    
    public List<BudgetCategory> getExpenseCategories() {
        return new ArrayList<>(expenseCategories);
    }

    public void addIncomeCategory(BudgetCategory category) {
        incomeCategories.add(category);
        saveData();
    }
    
    public void addExpenseCategory(BudgetCategory category) {
        expenseCategories.add(category);
        saveData();
    }

    public void updateIncomeCategory(BudgetCategory updatedCategory) {
        for (int i = 0; i < incomeCategories.size(); i++) {
            if (incomeCategories.get(i).getName().equals(updatedCategory.getName())) {
                incomeCategories.set(i, updatedCategory);
                break;
            }
        }
        saveData();
    }
    
    public void updateExpenseCategory(BudgetCategory updatedCategory) {
        for (int i = 0; i < expenseCategories.size(); i++) {
            if (expenseCategories.get(i).getName().equals(updatedCategory.getName())) {
                expenseCategories.set(i, updatedCategory);
                break;
            }
        }
        saveData();
    }

    public BudgetCategory getCategoryByName(String name) {
        for (BudgetCategory category : incomeCategories) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        for (BudgetCategory category : expenseCategories) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        return null;
    }

    // Budget Calculations
    public double getTotalSpentThisMonth() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
        
        double total = 0.0;
        for (Transaction transaction : transactions) {
            cal.setTime(transaction.getDate());
            if (cal.get(Calendar.YEAR) == currentYear && 
                cal.get(Calendar.MONTH) == currentMonth && 
                transaction.getType() == Transaction.TransactionType.EXPENSE) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getTotalIncomeThisMonth() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
        
        double total = 0.0;
        for (Transaction transaction : transactions) {
            cal.setTime(transaction.getDate());
            if (cal.get(Calendar.YEAR) == currentYear && 
                cal.get(Calendar.MONTH) == currentMonth && 
                transaction.getType() == Transaction.TransactionType.INCOME) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getRemainingBudget() {
        return getTotalIncomeThisMonth() - getTotalSpentThisMonth();
    }

    public double getMonthlySavings() {
        return getTotalIncomeThisMonth() - getTotalSpentThisMonth();
    }

    // Total across all time (not just current month)
    public double getTotalIncome() {
        double total = 0.0;
        for (Transaction transaction : transactions) {
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpenses() {
        double total = 0.0;
        for (Transaction transaction : transactions) {
            if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getTotalRemaining() {
        return getTotalIncome() - getTotalExpenses();
    }

    public double getSpentByCategory(String categoryName) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
        
        double total = 0.0;
        for (Transaction transaction : transactions) {
            cal.setTime(transaction.getDate());
            if (cal.get(Calendar.YEAR) == currentYear && 
                cal.get(Calendar.MONTH) == currentMonth && 
                transaction.getType() == Transaction.TransactionType.EXPENSE &&
                transaction.getCategory().equals(categoryName)) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getBudgetForCategory(String categoryName) {
        BudgetCategory category = getCategoryByName(categoryName);
        return category != null ? category.getBudget() : 0.0;
    }

    public double getRemainingBudgetForCategory(String categoryName) {
        return getBudgetForCategory(categoryName) - getSpentByCategory(categoryName);
    }

    // Monthly Budget Management
    public void setMonthlyBudget(double budget) {
        monthlyBudget = budget;
        saveData();
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    // Search and Filter
    public List<Transaction> searchTransactions(String query) {
        List<Transaction> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Transaction transaction : transactions) {
            if (transaction.getDescription().toLowerCase().contains(lowerQuery) ||
                transaction.getCategory().toLowerCase().contains(lowerQuery)) {
                results.add(transaction);
            }
        }
        return results;
    }

    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        Collections.sort(sorted, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    // Data Persistence
    private void saveData() {
        if (prefs == null) return;
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("monthly_budget", (float) monthlyBudget);
        editor.putInt("transaction_count", transactions.size());
        editor.putInt("income_category_count", incomeCategories.size());
        editor.putInt("expense_category_count", expenseCategories.size());
        editor.apply();
        
        // Save transactions
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            String prefix = "transaction_" + i;
            editor.putLong(prefix + "_id", t.getId());
            editor.putString(prefix + "_description", t.getDescription());
            editor.putFloat(prefix + "_amount", (float) t.getAmount());
            editor.putString(prefix + "_category", t.getCategory());
            editor.putLong(prefix + "_date", t.getDate().getTime());
            editor.putInt(prefix + "_type", t.getType() == Transaction.TransactionType.INCOME ? 1 : 0);
        }
        editor.apply();
    }

    private void loadData() {
        if (prefs == null) return;
        
        monthlyBudget = prefs.getFloat("monthly_budget", 0.0f);
        
        // Load transactions
        int transactionCount = prefs.getInt("transaction_count", 0);
        transactions.clear();
        for (int i = 0; i < transactionCount; i++) {
            String prefix = "transaction_" + i;
            long id = prefs.getLong(prefix + "_id", 0);
            String description = prefs.getString(prefix + "_description", "");
            double amount = prefs.getFloat(prefix + "_amount", 0.0f);
            String category = prefs.getString(prefix + "_category", "");
            long dateTime = prefs.getLong(prefix + "_date", System.currentTimeMillis());
            int typeInt = prefs.getInt(prefix + "_type", 0);
            
            Transaction.TransactionType type = typeInt == 1 ? Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;
            Transaction transaction = new Transaction(id, description, amount, category, new Date(dateTime), type);
            transactions.add(transaction);
        }
    }

    // Budget Category Class
    public static class BudgetCategory {
        private String name;
        private String color;
        private double budget;

        public BudgetCategory(String name, String color, double budget) {
            this.name = name;
            this.color = color;
            this.budget = budget;
        }

        public String getName() { return name; }
        public String getColor() { return color; }
        public double getBudget() { return budget; }
        
        public void setName(String name) { this.name = name; }
        public void setColor(String color) { this.color = color; }
        public void setBudget(double budget) { this.budget = budget; }
    }

    // Transaction Class
    public static class Transaction {
        public enum TransactionType {
            INCOME, EXPENSE
        }

        private long id;
        private String description;
        private double amount;
        private String category;
        private Date date;
        private TransactionType type;

        public Transaction(String description, double amount, String category, TransactionType type) {
            this.id = System.currentTimeMillis();
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = new Date();
            this.type = type;
        }

        public Transaction(long id, String description, double amount, String category, Date date, TransactionType type) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = date;
            this.type = type;
        }

        // Getters and Setters
        public long getId() { return id; }
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public String getCategory() { return category; }
        public Date getDate() { return date; }
        public TransactionType getType() { return type; }
        
        public void setId(long id) { this.id = id; }
        public void setDescription(String description) { this.description = description; }
        public void setAmount(double amount) { this.amount = amount; }
        public void setCategory(String category) { this.category = category; }
        public void setDate(Date date) { this.date = date; }
        public void setType(TransactionType type) { this.type = type; }
    }
}
