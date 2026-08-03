package com.example.autobookkeep.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT * FROM transactions WHERE date >= :start AND date <= :end ORDER BY date DESC")
    List<Transaction> getTransactionsByRange(long start, long end);

    @Query("SELECT SUM(amount) FROM transactions WHERE date >= :start AND date <= :end AND type = :type")
    Double getTotalAmountByType(long start, long end, int type);

    /**
     * Query historical categories by merchant or product keyword (fuzzy match on note/remark).
     * Used to infer category for newly recognized bills based on past records.
     * Ordered by date DESC so the most recent records take priority in voting.
     */
    @Query("SELECT category FROM transactions WHERE type = :type AND category IS NOT NULL AND category != '' " +
            "AND (note LIKE '%' || :keyword || '%' OR remark LIKE '%' || :keyword || '%') " +
            "ORDER BY date DESC LIMIT :limit")
    List<String> getCategoriesByKeywordSync(int type, String keyword, int limit);
}