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
     * Returns full transactions matching the keyword, ordered by date DESC.
     * Used for merged recency-weighted category learning together with the main bill_database.
     */
    @Query("SELECT * FROM transactions WHERE type = :type AND category IS NOT NULL AND category != '' " +
            "AND (note LIKE '%' || :keyword || '%' OR remark LIKE '%' || :keyword || '%') " +
            "ORDER BY date DESC LIMIT :limit")
    List<Transaction> getTransactionsByKeywordSync(int type, String keyword, int limit);

    /** Renames a category across all auto-tracked transactions (kept in sync with BillDao.renameCategory). */
    @Query("UPDATE transactions SET category = :newName WHERE category = :oldName")
    int renameCategory(String oldName, String newName);

    /** Clears all auto-tracked transactions (learning records), used when the user wipes all bills. */
    @Query("DELETE FROM transactions")
    void deleteAll();

    /** Deletes auto-tracked transactions older than the given time (kept in sync with deleteBillsBefore). */
    @Query("DELETE FROM transactions WHERE date < :beforeTime")
    int deleteBefore(long beforeTime);
}