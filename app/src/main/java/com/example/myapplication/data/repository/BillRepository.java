package com.example.myapplication.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.myapplication.data.dao.BillDao;
import com.example.myapplication.data.dao.BudgetDao;
import com.example.myapplication.data.dao.DaySum;
import com.example.myapplication.data.dao.TypeSum;
import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.entity.Budget;
import com.example.myapplication.data.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for bill and budget data.
 * Fragments should talk to this, never directly to DAOs.
 */
public class BillRepository {

    private final BillDao billDao;
    private final BudgetDao budgetDao;
    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BillRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        billDao = db.billDao();
        budgetDao = db.budgetDao();
        userDao = db.userDao();
    }

    // ── Bill CRUD ──────────────────────────────────────────────

    public void insert(Bill bill) {
        executor.execute(() -> billDao.insert(bill));
    }

    public void update(Bill bill) {
        executor.execute(() -> billDao.update(bill));
    }

    public void delete(Bill bill) {
        executor.execute(() -> billDao.delete(bill));
    }

    public LiveData<List<Bill>> getAllBills(long userId) {
        return billDao.getAllBills(userId);
    }

    public LiveData<List<Bill>> getBillsBetween(long userId, long start, long end) {
        return billDao.getBillsBetween(userId, start, end);
    }

    public LiveData<Double> getIncomeBetween(long userId, long start, long end) {
        return billDao.getIncomeBetween(userId, start, end);
    }

    public LiveData<Double> getExpenseBetween(long userId, long start, long end) {
        return billDao.getExpenseBetween(userId, start, end);
    }

    public LiveData<List<TypeSum>> getExpenseByType(long userId, long start, long end) {
        return billDao.getExpenseByType(userId, start, end);
    }

    public LiveData<List<TypeSum>> getIncomeByType(long userId, long start, long end) {
        return billDao.getIncomeByType(userId, start, end);
    }

    public LiveData<List<DaySum>> getDailySum(long userId, long start, long end) {
        return billDao.getDailySum(userId, start, end);
    }

    public void deleteAllBills(long userId) {
        executor.execute(() -> billDao.deleteAllBills(userId));
    }

    public int deleteBillsBefore(long userId, long beforeTime) {
        final int[] result = {0};
        try {
            Thread t = new Thread(() -> {
                result[0] = billDao.deleteBillsBefore(userId, beforeTime);
            });
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    // ── Budget ──────────────────────────────────────────────────

    public void saveBudget(Budget budget) {
        executor.execute(() -> budgetDao.insert(budget));
    }

    public LiveData<Budget> getBudget(String yearMonth, long userId) {
        return budgetDao.getBudget(yearMonth, userId);
    }

    // ── User ──────────────────────────────────────────────────────

    public void insertUser(User user, final OnUserInsertedListener listener) {
        executor.execute(() -> {
            long id = userDao.insert(user);
            if (listener != null) {
                listener.onInserted(id);
            }
        });
    }

    public interface OnUserInsertedListener {
        void onInserted(long userId);
    }

    public void deleteUser(long userId) {
        executor.execute(() -> userDao.deleteById(userId));
    }

    public void updateUser(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public User login(String username, String password) {
        return userDao.login(username, password);
    }

    public User getUserById(long id) {
        return userDao.getUserById(id);
    }

    public User getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    public User getAdminUser() {
        return userDao.getAdminUser();
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public List<User> getAllUsersSync() {
        return userDao.getAllUsersSync();
    }
}
