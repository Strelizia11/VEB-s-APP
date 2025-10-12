package com.example.veb_app.data;

import android.content.Context;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {
    private TransactionDao transactionDao;
    private ExecutorService executor;
    
    public TransactionRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        transactionDao = database.transactionDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public void getAllTransactions(DataCallback<List<TransactionEntity>> callback) {
        executor.execute(() -> {
            try {
                List<TransactionEntity> transactions = transactionDao.getAllTransactions();
                callback.onSuccess(transactions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTransactionById(int id, DataCallback<TransactionEntity> callback) {
        executor.execute(() -> {
            try {
                TransactionEntity transaction = transactionDao.getTransactionById(id);
                callback.onSuccess(transaction);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTransactionsByType(String type, DataCallback<List<TransactionEntity>> callback) {
        executor.execute(() -> {
            try {
                List<TransactionEntity> transactions = transactionDao.getTransactionsByType(type);
                callback.onSuccess(transactions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTransactionsByCategory(String category, DataCallback<List<TransactionEntity>> callback) {
        executor.execute(() -> {
            try {
                List<TransactionEntity> transactions = transactionDao.getTransactionsByCategory(category);
                callback.onSuccess(transactions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTransactionsByDateRange(Date startDate, Date endDate, DataCallback<List<TransactionEntity>> callback) {
        executor.execute(() -> {
            try {
                long start = startDate.getTime();
                long end = endDate.getTime();
                List<TransactionEntity> transactions = transactionDao.getTransactionsByDateRange(start, end);
                callback.onSuccess(transactions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTransactionsByDateRangeAndType(Date startDate, Date endDate, String type, DataCallback<List<TransactionEntity>> callback) {
        executor.execute(() -> {
            try {
                long start = startDate.getTime();
                long end = endDate.getTime();
                List<TransactionEntity> transactions = transactionDao.getTransactionsByDateRangeAndType(start, end, type);
                callback.onSuccess(transactions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void searchTransactions(String query, DataCallback<List<TransactionEntity>> callback) {
        executor.execute(() -> {
            try {
                List<TransactionEntity> transactions = transactionDao.searchTransactions(query);
                callback.onSuccess(transactions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTotalIncomeForDateRange(Date startDate, Date endDate, DataCallback<Double> callback) {
        executor.execute(() -> {
            try {
                long start = startDate.getTime();
                long end = endDate.getTime();
                Double total = transactionDao.getTotalIncomeForDateRange(start, end);
                callback.onSuccess(total != null ? total : 0.0);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTotalExpensesForDateRange(Date startDate, Date endDate, DataCallback<Double> callback) {
        executor.execute(() -> {
            try {
                long start = startDate.getTime();
                long end = endDate.getTime();
                Double total = transactionDao.getTotalExpensesForDateRange(start, end);
                callback.onSuccess(total != null ? total : 0.0);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTotalIncome(DataCallback<Double> callback) {
        executor.execute(() -> {
            try {
                Double total = transactionDao.getTotalIncome();
                callback.onSuccess(total != null ? total : 0.0);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTotalExpenses(DataCallback<Double> callback) {
        executor.execute(() -> {
            try {
                Double total = transactionDao.getTotalExpenses();
                callback.onSuccess(total != null ? total : 0.0);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void insertTransaction(TransactionEntity transaction, DataCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long id = transactionDao.insertTransaction(transaction);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateTransaction(TransactionEntity transaction, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                transactionDao.updateTransaction(transaction);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteTransaction(TransactionEntity transaction, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                transactionDao.deleteTransaction(transaction);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteTransactionById(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                transactionDao.deleteTransactionById(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTransactionsCount(DataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = transactionDao.getTransactionsCount();
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getCategoriesByType(String type, DataCallback<List<String>> callback) {
        executor.execute(() -> {
            try {
                List<String> categories = transactionDao.getCategoriesByType(type);
                callback.onSuccess(categories);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    // Helper methods for monthly calculations
    public void getCurrentMonthIncome(DataCallback<Double> callback) {
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
        
        getTotalIncomeForDateRange(startOfMonth, endOfMonth, callback);
    }
    
    public void getCurrentMonthExpenses(DataCallback<Double> callback) {
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
        
        getTotalExpensesForDateRange(startOfMonth, endOfMonth, callback);
    }
    
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception error);
    }
}
