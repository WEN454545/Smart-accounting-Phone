package com.example.myapplication.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.entity.Budget;
import com.example.myapplication.data.repository.BillRepository;

import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final BillRepository repository;
    private final long userId;

    private final MutableLiveData<Integer> year = new MutableLiveData<>();
    private final MutableLiveData<Integer> month = new MutableLiveData<>();

    private final MediatorLiveData<List<Bill>> bills = new MediatorLiveData<>();
    private final MediatorLiveData<Double> income = new MediatorLiveData<>();
    private final MediatorLiveData<Double> expense = new MediatorLiveData<>();
    private final MediatorLiveData<Budget> budget = new MediatorLiveData<>();

    private LiveData<List<Bill>> currentBillsSource;
    private LiveData<Double> currentIncomeSource;
    private LiveData<Double> currentExpenseSource;
    private LiveData<Budget> currentBudgetSource;

    private long monthStart;
    private long monthEnd;
    private String yearMonth;

    public HomeViewModel(Application application) {
        super(application);
        repository = MyApplication.getRepository();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getUserId();

        Calendar cal = Calendar.getInstance();
        year.setValue(cal.get(Calendar.YEAR));
        month.setValue(cal.get(Calendar.MONTH));

        setMonthRange();
        bindSources();
    }

    public LiveData<Integer> getYear() { return year; }
    public LiveData<Integer> getMonth() { return month; }

    public LiveData<List<Bill>> getBills() { return bills; }
    public LiveData<Double> getIncome() { return income; }
    public LiveData<Double> getExpense() { return expense; }
    public LiveData<Budget> getBudget() { return budget; }

    public long getUserId() { return userId; }
    public long getMonthStart() { return monthStart; }
    public long getMonthEnd() { return monthEnd; }
    public String getYearMonth() { return yearMonth; }

    public void goToPreviousMonth() {
        Integer m = month.getValue();
        Integer y = year.getValue();
        if (m == null || y == null) return;
        if (m == 0) { month.setValue(11); year.setValue(y - 1); }
        else { month.setValue(m - 1); }
        reloadMonth();
    }

    public void goToNextMonth() {
        Integer m = month.getValue();
        Integer y = year.getValue();
        if (m == null || y == null) return;
        if (m == 11) { month.setValue(0); year.setValue(y + 1); }
        else { month.setValue(m + 1); }
        reloadMonth();
    }

    public void goToYearMonth(int y, int m) {
        year.setValue(y);
        month.setValue(m);
        reloadMonth();
    }

    private void setMonthRange() {
        Integer y = year.getValue();
        Integer m = month.getValue();
        if (y == null || m == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(y, m, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        monthStart = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        monthEnd = cal.getTimeInMillis();

        yearMonth = y + "-" + String.format("%02d", m + 1);
    }

    private void bindSources() {
        currentBillsSource = repository.getBillsBetween(userId, monthStart, monthEnd);
        currentIncomeSource = repository.getIncomeBetween(userId, monthStart, monthEnd);
        currentExpenseSource = repository.getExpenseBetween(userId, monthStart, monthEnd);
        currentBudgetSource = repository.getBudget(yearMonth, userId);

        bills.addSource(currentBillsSource, bills::setValue);
        income.addSource(currentIncomeSource, income::setValue);
        expense.addSource(currentExpenseSource, expense::setValue);
        budget.addSource(currentBudgetSource, budget::setValue);
    }

    private void reloadMonth() {
        setMonthRange();

        if (currentBillsSource != null) bills.removeSource(currentBillsSource);
        if (currentIncomeSource != null) income.removeSource(currentIncomeSource);
        if (currentExpenseSource != null) expense.removeSource(currentExpenseSource);
        if (currentBudgetSource != null) budget.removeSource(currentBudgetSource);

        currentBillsSource = repository.getBillsBetween(userId, monthStart, monthEnd);
        currentIncomeSource = repository.getIncomeBetween(userId, monthStart, monthEnd);
        currentExpenseSource = repository.getExpenseBetween(userId, monthStart, monthEnd);
        currentBudgetSource = repository.getBudget(yearMonth, userId);

        bills.addSource(currentBillsSource, bills::setValue);
        income.addSource(currentIncomeSource, income::setValue);
        expense.addSource(currentExpenseSource, expense::setValue);
        budget.addSource(currentBudgetSource, budget::setValue);
    }

    public void insertBill(Bill bill) {
        bill.setUserId(userId);
        repository.insert(bill);
    }

    public void updateBill(Bill bill) {
        repository.update(bill);
    }

    public void deleteBill(Bill bill) {
        repository.delete(bill);
    }

    public void saveBudget(Budget budget) {
        budget.setUserId(userId);
        repository.saveBudget(budget);
    }
}