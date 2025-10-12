package com.example.veb_app.data;

import androidx.room.*;
import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, createdDate DESC")
    List<TransactionEntity> getAllTransactions();
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getTransactionById(int id);
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY transactionDate DESC")
    List<TransactionEntity> getTransactionsByType(String type);
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY transactionDate DESC")
    List<TransactionEntity> getTransactionsByCategory(String category);
    
    @Query("SELECT * FROM transactions WHERE transactionDate >= :startDate AND transactionDate <= :endDate ORDER BY transactionDate DESC")
    List<TransactionEntity> getTransactionsByDateRange(long startDate, long endDate);
    
    @Query("SELECT * FROM transactions WHERE transactionDate >= :startDate AND transactionDate <= :endDate AND type = :type ORDER BY transactionDate DESC")
    List<TransactionEntity> getTransactionsByDateRangeAndType(long startDate, long endDate, String type);
    
    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY transactionDate DESC")
    List<TransactionEntity> searchTransactions(String query);
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND transactionDate >= :startDate AND transactionDate <= :endDate")
    Double getTotalIncomeForDateRange(long startDate, long endDate);
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND transactionDate >= :startDate AND transactionDate <= :endDate")
    Double getTotalExpensesForDateRange(long startDate, long endDate);
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    Double getTotalIncome();
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    Double getTotalExpenses();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTransaction(TransactionEntity transaction);
    
    @Update
    void updateTransaction(TransactionEntity transaction);
    
    @Delete
    void deleteTransaction(TransactionEntity transaction);
    
    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteTransactionById(int id);
    
    @Query("SELECT COUNT(*) FROM transactions")
    int getTransactionsCount();
    
    @Query("SELECT DISTINCT category FROM transactions WHERE type = :type ORDER BY category")
    List<String> getCategoriesByType(String type);
}
