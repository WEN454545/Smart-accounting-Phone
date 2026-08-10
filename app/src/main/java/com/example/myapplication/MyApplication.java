package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.data.entity.User;
import com.example.myapplication.data.repository.BillRepository;
import com.example.myapplication.util.ColorSchemeManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application class - holds app-wide singletons.
 */
public class MyApplication extends Application {

    private static BillRepository repository;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static int themeResId;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new BillRepository(this);
        ensureAdminExists();

        // Initialize theme from saved preference
        themeResId = ColorSchemeManager.getThemeResId(this);
    }

    /** Returns the theme resource ID for the current color scheme */
    public static int getThemeResId() {
        return themeResId;
    }

    private void ensureAdminExists() {
        executor.execute(() -> {
            User admin = repository.getAdminUser();
            if (admin == null) {
                User newAdmin = new User("admin", "123456", true, System.currentTimeMillis());
                repository.insertUser(newAdmin, null);
            }
        });
    }

    public static BillRepository getRepository() {
        return repository;
    }
}