package com.example.myapplication.ui.calendar;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.dao.DaySum;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.repository.BillRepository;

import java.util.Calendar;
import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private final BillRepository repository;
    private final long userId;

    private final MutableLiveData<Integer> year = new MutableLiveData<>();
    private final MutableLiveData<Integer> month = new MutableLiveData<>();

    private final MediatorLiveData<Double> income = new MediatorLiveData<>();
    private final MediatorLiveData<Double> expense = new MediatorLiveData<>();
    private final MediatorLiveData<List<DaySum>> dailySums = new MediatorLiveData<>();

    private final MutableLiveData<Integer> selectedDay = new MutableLiveData<>();
    private final MediatorLiveData<List<Bill>> billsForDay = new MediatorLiveData<>();

    private LiveData<Double> currentIncomeSource;
    private LiveData<Double> currentExpenseSource;
    private LiveData<List<DaySum>> currentDailySumsSource;
    private LiveData<List<Bill>> currentBillsSource;

    public CalendarViewModel(Application application) {
        super(application);
        repository = MyApplication.getRepository();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getUserId();

        Calendar cal = Calendar.getInstance();
        year.setValue(cal.get(Calendar.YEAR));
        month.setValue(cal.get(Calendar.MONTH));

        loadMonthData();
    }

    public LiveData<Integer> getYear() { return year; }
    public LiveData<Integer> getMonth() { return month; }

    public LiveData<Double> getIncome() { return income; }
    public LiveData<Double> getExpense() { return expense; }
    public LiveData<List<DaySum>> getDailySums() { return dailySums; }

    public LiveData<Integer> getSelectedDay() { return selectedDay; }
    public LiveData<List<Bill>> getBillsForDay() { return billsForDay; }

    /**
     * Force re-query month data by recreating Room LiveData sources.
     * Call this when the Fragment comes back to foreground to pick up
     * changes missed while in background (when MediatorLiveData was inactive).
     */
    public void reloadMonthData() {
        loadMonthData();
    }

    public void goToPreviousMonth() {
        Integer m = month.getValue();
        Integer y = year.getValue();
        if (m == null || y == null) return;
        if (m == 0) { month.setValue(11); year.setValue(y - 1); }
        else { month.setValue(m - 1); }
        loadMonthData();
    }

    public void goToNextMonth() {
        Integer m = month.getValue();
        Integer y = year.getValue();
        if (m == null || y == null) return;
        if (m == 11) { month.setValue(0); year.setValue(y + 1); }
        else { month.setValue(m + 1); }
        loadMonthData();
    }

    private void loadMonthData() {
        Integer y = year.getValue();
        Integer m = month.getValue();
        if (y == null || m == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(y, m, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis();

        LiveData<Double> newIncome = repository.getIncomeBetween(userId, start, end);
        LiveData<Double> newExpense = repository.getExpenseBetween(userId, start, end);
        LiveData<List<DaySum>> newDailySums = repository.getDailySum(userId, start, end);

        if (currentIncomeSource != null) {
            income.removeSource(currentIncomeSource);
        }
        if (currentExpenseSource != null) {
            expense.removeSource(currentExpenseSource);
        }
        if (currentDailySumsSource != null) {
            dailySums.removeSource(currentDailySumsSource);
        }

        currentIncomeSource = newIncome;
        currentExpenseSource = newExpense;
        currentDailySumsSource = newDailySums;

        income.addSource(newIncome, new Observer<Double>() {
            @Override
            public void onChanged(Double value) {
                income.setValue(value);
            }
        });

        expense.addSource(newExpense, new Observer<Double>() {
            @Override
            public void onChanged(Double value) {
                expense.setValue(value);
            }
        });

        dailySums.addSource(newDailySums, new Observer<List<DaySum>>() {
            @Override
            public void onChanged(List<DaySum> value) {
                dailySums.setValue(value);
            }
        });
    }

    public void deleteBill(Bill bill) {
        repository.delete(bill);
    }

    public void selectDay(int day) {
        selectedDay.setValue(day);
        loadBillsForDay(day);
    }

    public void clearSelection() {
        selectedDay.setValue(null);
        if (currentBillsSource != null) {
            billsForDay.removeSource(currentBillsSource);
            currentBillsSource = null;
        }
        billsForDay.setValue(null);
    }

    private void loadBillsForDay(int day) {
        Integer y = year.getValue();
        Integer m = month.getValue();
        if (y == null || m == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(y, m, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long dayEnd = cal.getTimeInMillis();

        LiveData<List<Bill>> newSource = repository.getBillsBetween(userId, dayStart, dayEnd);

        if (currentBillsSource != null) {
            billsForDay.removeSource(currentBillsSource);
        }

        currentBillsSource = newSource;
        billsForDay.addSource(newSource, new Observer<List<Bill>>() {
            @Override
            public void onChanged(List<Bill> value) {
                billsForDay.setValue(value);
            }
        });
    }

    public static long[] getMonthRange(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis();
        return new long[] { start, end };
    }
}
