package com.example.myapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.Bill;

import java.util.List;

@Dao
public interface BillDao {
    @Insert
    void insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bill);

    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Bill>> getAllBills(long userId);

    @Query("SELECT * FROM bills WHERE userId = :userId AND timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    LiveData<List<Bill>> getBillsBetween(long userId, long start, long end);

    @Query("SELECT SUM(amount) FROM bills WHERE userId = :userId AND category = 'income' AND timestamp >= :start AND timestamp < :end")
    LiveData<Double> getIncomeBetween(long userId, long start, long end);

    @Query("SELECT SUM(ABS(amount)) FROM bills WHERE userId = :userId AND category = 'expense' AND timestamp >= :start AND timestamp < :end")
    LiveData<Double> getExpenseBetween(long userId, long start, long end);

    @Query("SELECT type, SUM(ABS(amount)) as total FROM bills WHERE userId = :userId AND category = 'expense' AND timestamp >= :start AND timestamp < :end GROUP BY type")
    LiveData<List<TypeSum>> getExpenseByType(long userId, long start, long end);

    @Query("SELECT type, SUM(amount) as total FROM bills WHERE userId = :userId AND category = 'income' AND timestamp >= :start AND timestamp < :end GROUP BY type")
    LiveData<List<TypeSum>> getIncomeByType(long userId, long start, long end);

    @Query("SELECT strftime('%d', datetime(timestamp/1000, 'unixepoch', '+8 hours')) as day, " +
           "SUM(CASE WHEN category = 'income' THEN amount ELSE 0 END) as income, " +
           "SUM(CASE WHEN category = 'expense' THEN ABS(amount) ELSE 0 END) as expense " +
           "FROM bills WHERE userId = :userId AND timestamp >= :start AND timestamp < :end GROUP BY day")
    LiveData<List<DaySum>> getDailySum(long userId, long start, long end);

    @Query("DELETE FROM bills WHERE userId = :userId AND timestamp < :beforeTime")
    int deleteBillsBefore(long userId, long beforeTime);

    @Query("DELETE FROM bills WHERE userId = :userId")
    int deleteAllBills(long userId);

    /** Renames a category across all bills (all users, category presets are global). */
    @Query("UPDATE bills SET type = :newName WHERE type = :oldName")
    int renameCategory(String oldName, String newName);

    /** Returns recent bill category names whose note contains the keyword (for CSV import inference). */
    @Query("SELECT type FROM bills WHERE userId = :userId AND category = :billCategory " +
           "AND note LIKE '%' || :keyword || '%' ORDER BY timestamp DESC LIMIT :limit")
    List<String> getCategoriesByNoteKeyword(long userId, String billCategory, String keyword, int limit);
}
