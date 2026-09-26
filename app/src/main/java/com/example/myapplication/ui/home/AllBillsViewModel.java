package com.example.myapplication.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.repository.BillRepository;

import java.util.ArrayList;
import java.util.List;

public class AllBillsViewModel extends AndroidViewModel {

    private final BillRepository repository;
    private final LiveData<List<Bill>> allBills;
    private final MediatorLiveData<List<Bill>> filteredBills = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<BillFilter> filter = new MutableLiveData<>(new BillFilter());
    private final long userId;
    private List<Bill> sourceBills = new ArrayList<>();

    public AllBillsViewModel(@NonNull Application application) {
        super(application);
        repository = MyApplication.getRepository();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getUserId();
        allBills = repository.getAllBills(userId);
        // Recompute the displayed list whenever the source data, search query or filter changes
        filteredBills.addSource(allBills, bills -> {
            sourceBills = bills != null ? bills : new ArrayList<>();
            recompute();
        });
        filteredBills.addSource(query, q -> recompute());
        filteredBills.addSource(filter, f -> recompute());
    }

    private void recompute() {
        filteredBills.setValue(BillFilter.apply(sourceBills, query.getValue(), filter.getValue()));
    }

    public LiveData<List<Bill>> getFilteredBills() {
        return filteredBills;
    }

    /** Current filter state (for reopening the filter sheet). Never null. */
    public BillFilter getFilter() {
        BillFilter f = filter.getValue();
        return f != null ? f : new BillFilter();
    }

    public void setQuery(String q) {
        query.setValue(q);
    }

    /** Current search query (for restoring the input box after recreation). Never null. */
    public String getQueryValue() {
        String q = query.getValue();
        return q != null ? q : "";
    }

    public void setFilter(BillFilter f) {
        filter.setValue(f != null ? f : new BillFilter());
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
