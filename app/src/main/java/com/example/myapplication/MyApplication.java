package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.data.entity.User;
import com.example.myapplication.data.repository.BillRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application class - holds app-wide singletons.
 */
public class MyApplication extends Application {

    private static BillRepository repository;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new BillRepository(this);
        ensureAdminExists();
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