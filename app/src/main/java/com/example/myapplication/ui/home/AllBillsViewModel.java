package com.example.myapplication.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.repository.BillRepository;

import java.util.List;

public class AllBillsViewModel extends AndroidViewModel {

    private final BillRepository repository;
    private final LiveData<List<Bill>> allBills;
    private final long userId;

    public AllBillsViewModel(@NonNull Application application) {
        super(application);
        repository = MyApplication.getRepository();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getUserId();
        allBills = repository.getAllBills(userId);
    }

    public LiveData<List<Bill>> getAllBills() {
        return allBills;
    }

    public void updateBill(Bill bill) {
        repository.update(bill);
    }

    public void deleteBill(Bill bill) {
        repository.delete(bill);
    }

    public void deleteAllBills() {
        repository.deleteAllBills(userId);
    }
}
