package com.example.myapplication.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.data.dao.BillDao;
import com.example.myapplication.data.dao.BudgetDao;
import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.entity.Budget;
import com.example.myapplication.data.entity.User;

@Database(entities = {Bill.class, Budget.class, User.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract BillDao billDao();
    public abstract BudgetDao budgetDao();
    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "bill_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
