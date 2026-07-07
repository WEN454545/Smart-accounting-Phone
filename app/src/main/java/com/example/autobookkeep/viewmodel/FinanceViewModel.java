package com.example.autobookkeep.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.autobookkeep.BackupManager;
import com.example.autobookkeep.database.AppDatabase;
import com.example.autobookkeep.database.AssetAccount;
import com.example.autobookkeep.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final MutableLiveData<List<Transaction>> allTransactions = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> monthTransactions = new MutableLiveData<>();
    private final MutableLiveData<List<AssetAccount>> allAssets = new MutableLiveData<>();
    private final MutableLiveData<Double> monthExpense = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> monthIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalAssets = new MutableLiveData<>(0.0);

    public FinanceViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        loadData();
    }

    public LiveData<List<Transaction>> getAllTransactions() { return allTransactions; }
    public LiveData<List<Transaction>> getMonthTransactions() { return monthTransactions; }
    public LiveData<List<AssetAccount>> getAllAssets() { return allAssets; }
    public LiveData<Double> getMonthExpense() { return monthExpense; }
    public LiveData<Double> getMonthIncome() { return monthIncome; }
    public LiveData<Double> getTotalAssets() { return totalAssets; }

    public void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Transaction> txs = db.transactionDao().getAllTransactionsSync();
            allTransactions.postValue(txs);
            allAssets.postValue(db.assetAccountDao().getAllSync());

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long monthStart = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, 1);
            long monthEnd = cal.getTimeInMillis();

            double expense = 0, income = 0;
            List<Transaction> monthList = new java.util.ArrayList<>();
            for (Transaction t : txs) {
                if (t.date >= monthStart && t.date < monthEnd) {
                    monthList.add(t);
                    if (t.type == 0) expense += t.amount;
                    else income += t.amount;
                }
            }
            monthTransactions.postValue(monthList);
            monthExpense.postValue(expense);
            monthIncome.postValue(income);

            double assetTotal = 0;
            for (AssetAccount a : db.assetAccountDao().getAllSync()) {
                assetTotal += a.amount;
            }
            totalAssets.postValue(assetTotal);
        });
    }

    public void addTransaction(Transaction t) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.transactionDao().insert(t);
            loadData();
            BackupManager.triggerAutoUploadIfEnabled(getApplication());
        });
    }

    public void addTransactionWithAssetSync(Transaction t) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.transactionDao().insert(t);
            if (t.assetId > 0) {
                AssetAccount asset = db.assetAccountDao().getAllSync().stream()
                        .filter(a -> a.id == t.assetId).findFirst().orElse(null);
                if (asset != null) {
                    if (t.type == 0) asset.amount -= t.amount;
                    else asset.amount += t.amount;
                    asset.updateTime = System.currentTimeMillis();
                    db.assetAccountDao().update(asset);
                }
            }
            loadData();
            BackupManager.triggerAutoUploadIfEnabled(getApplication());
        });
    }

    public void deleteTransaction(Transaction t) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.transactionDao().delete(t);
            loadData();
            BackupManager.triggerAutoUploadIfEnabled(getApplication());
        });
    }

    public void addAsset(AssetAccount a) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.assetAccountDao().insert(a);
            loadData();
        });
    }

    public void updateAsset(AssetAccount a) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.assetAccountDao().update(a);
            loadData();
        });
    }

    public void deleteAsset(AssetAccount a) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.assetAccountDao().delete(a);
            loadData();
        });
    }
}