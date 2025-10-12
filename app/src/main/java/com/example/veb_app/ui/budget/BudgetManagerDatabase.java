package com.example.veb_app.ui.budget;

import android.content.Context;
import com.example.veb_app.data.TransactionEntity;
import com.example.veb_app.data.TransactionRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BudgetManagerDatabase {
    private static BudgetManagerDatabase instance;
    private TransactionRepository transactionRepository;
    private List<BudgetManager.Transaction> transactions;
    private Context context;
    
    private BudgetManagerDatabase(Context context) {
        this.context = context.getApplicationContext();
        this.transactionRepository = new TransactionRepository(this.context);
        this.transactions = new ArrayList<>();
        loadTransactions();
    }
    
    public static synchronized BudgetManagerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new BudgetManagerDatabase(context);
        }
        return instance;
    }
    
    private void loadTransactions() {
        transactionRepository.getAllTransactions(new TransactionRepository.DataCallback<List<TransactionEntity>>() {
            @Override
            public void onSuccess(List<TransactionEntity> transactionEntities) {
                transactions.clear();
                for (TransactionEntity entity : transactionEntities) {
                    transactions.add(convertEntityToTransaction(entity));
                }
                Collections.sort(transactions, (t1, t2) -> 
                    Long.compare(t2.getDate().getTime(), t1.getDate().getTime()));
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("BudgetManagerDatabase", "Error loading transactions", error);
            }
        });
    }
    
    public void addTransaction(BudgetManager.Transaction transaction, Callback callback) {
        TransactionEntity entity = convertTransactionToEntity(transaction);
        transactionRepository.insertTransaction(entity, new TransactionRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                // Create a new transaction with the database ID
                BudgetManager.Transaction newTransaction = new BudgetManager.Transaction(
                    id,
                    transaction.getDescription(),
                    transaction.getAmount(),
                    transaction.getCategory(),
                    transaction.getDate(),
                    transaction.getType()
                );
                transactions.add(newTransaction);
                sortTransactions();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("BudgetManagerDatabase", "Error adding transaction", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void updateTransaction(BudgetManager.Transaction transaction, Callback callback) {
        TransactionEntity entity = convertTransactionToEntity(transaction);
        transactionRepository.updateTransaction(entity, new TransactionRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Update local list
                for (int i = 0; i < transactions.size(); i++) {
                    if (transactions.get(i).getId() == transaction.getId()) {
                        transactions.set(i, transaction);
                        break;
                    }
                }
                sortTransactions();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("BudgetManagerDatabase", "Error updating transaction", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void deleteTransaction(BudgetManager.Transaction transaction, Callback callback) {
        TransactionEntity entity = convertTransactionToEntity(transaction);
        transactionRepository.deleteTransaction(entity, new TransactionRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                transactions.removeIf(t -> t.getId() == transaction.getId());
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("BudgetManagerDatabase", "Error deleting transaction", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public List<BudgetManager.Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }
    
    public List<BudgetManager.Transaction> getTransactionsByType(BudgetManager.Transaction.TransactionType type) {
        List<BudgetManager.Transaction> filtered = new ArrayList<>();
        for (BudgetManager.Transaction transaction : transactions) {
            if (transaction.getType() == type) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }
    
    public double getTotalIncome() {
        double total = 0.0;
        for (BudgetManager.Transaction transaction : transactions) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME) {
                total += transaction.getAmount();
            }
        }
        return total;
    }
    
    public double getTotalExpenses() {
        double total = 0.0;
        for (BudgetManager.Transaction transaction : transactions) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.EXPENSE) {
                total += transaction.getAmount();
            }
        }
        return total;
    }
    
    public double getTotalIncomeThisMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = calendar.getTime();
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfMonth = calendar.getTime();
        
        double total = 0.0;
        for (BudgetManager.Transaction transaction : transactions) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME &&
                transaction.getDate().compareTo(startOfMonth) >= 0 &&
                transaction.getDate().compareTo(endOfMonth) <= 0) {
                total += transaction.getAmount();
            }
        }
        return total;
    }
    
    public double getTotalSpentThisMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = calendar.getTime();
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfMonth = calendar.getTime();
        
        double total = 0.0;
        for (BudgetManager.Transaction transaction : transactions) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.EXPENSE &&
                transaction.getDate().compareTo(startOfMonth) >= 0 &&
                transaction.getDate().compareTo(endOfMonth) <= 0) {
                total += transaction.getAmount();
            }
        }
        return total;
    }
    
    public double getRemainingBudget() {
        return getTotalIncomeThisMonth() - getTotalSpentThisMonth();
    }
    
    public List<String> getIncomeCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("From Mama");
        categories.add("From Papa");
        categories.add("From Work");
        categories.add("Other");
        return categories;
    }
    
    public List<String> getExpenseCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Food & Dining");
        categories.add("Transportation");
        categories.add("Shopping");
        categories.add("Entertainment");
        categories.add("Bills & Utilities");
        categories.add("Healthcare");
        categories.add("Education");
        categories.add("Travel");
        categories.add("Personal Care");
        categories.add("Other");
        return categories;
    }
    
    public List<BudgetManager.Transaction> getTransactionsForDate(Date date) {
        List<BudgetManager.Transaction> result = new ArrayList<>();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(date);
        
        for (BudgetManager.Transaction transaction : transactions) {
            Calendar transactionCal = Calendar.getInstance();
            transactionCal.setTime(transaction.getDate());
            
            if (targetCal.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) &&
                targetCal.get(Calendar.MONTH) == transactionCal.get(Calendar.MONTH) &&
                targetCal.get(Calendar.DAY_OF_MONTH) == transactionCal.get(Calendar.DAY_OF_MONTH)) {
                result.add(transaction);
            }
        }
        
        return result;
    }
    
    public void refreshTransactions(Callback callback) {
        loadTransactions();
        if (callback != null) callback.onSuccess();
    }
    
    private void sortTransactions() {
        Collections.sort(transactions, (t1, t2) -> 
            Long.compare(t2.getDate().getTime(), t1.getDate().getTime()));
    }
    
    private TransactionEntity convertTransactionToEntity(BudgetManager.Transaction transaction) {
        TransactionEntity entity = new TransactionEntity(
            transaction.getDescription(),
            transaction.getCategory(),
            transaction.getAmount(),
            transaction.getType().toString(),
            transaction.getDate().getTime()
        );
        entity.setId((int) transaction.getId());
        entity.setCreatedDate(System.currentTimeMillis());
        entity.setModifiedDate(System.currentTimeMillis());
        return entity;
    }
    
    private BudgetManager.Transaction convertEntityToTransaction(TransactionEntity entity) {
        BudgetManager.Transaction.TransactionType type = entity.getType().equals("INCOME") ? 
            BudgetManager.Transaction.TransactionType.INCOME : BudgetManager.Transaction.TransactionType.EXPENSE;
        
        BudgetManager.Transaction transaction = new BudgetManager.Transaction(
            entity.getId(),
            entity.getDescription(),
            entity.getAmount(),
            entity.getCategory(),
            entity.getTransactionDateAsDate(),
            type
        );
        return transaction;
    }
    
    public interface Callback {
        void onSuccess();
        void onError(Exception error);
    }
}
