package com.example.myapplication.ui.stats;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.dao.DaySum;
import com.example.myapplication.data.dao.TypeSum;
import com.example.myapplication.data.repository.BillRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsViewModel extends AndroidViewModel {

    private final BillRepository repository;
    private final long userId;

    public enum Period { WEEK, MONTH, YEAR }

    private final MutableLiveData<Period> period = new MutableLiveData<>(Period.MONTH);

    // 年月状态（所有 Tab 共用）
    private final MutableLiveData<Integer> statsYear = new MutableLiveData<>();
    private final MutableLiveData<Integer> statsMonth = new MutableLiveData<>(); // 0-11
    private final MutableLiveData<Integer> statsWeekOffset = new MutableLiveData<>(0); // 相对于当前周的偏移

    private final MediatorLiveData<List<TypeSum>> expenseByType = new MediatorLiveData<>();
    private final MediatorLiveData<List<TypeSum>> incomeByType = new MediatorLiveData<>();
    private final MediatorLiveData<List<PeriodSum>> trendData = new MediatorLiveData<>();

    private LiveData<List<TypeSum>> currentExpenseByTypeSource;
    private LiveData<List<TypeSum>> currentIncomeByTypeSource;
    private LiveData<List<DaySum>> currentDailySource;

    public static class PeriodSum {
        public String label;
        public double income;
        public double expense;
        public boolean isCurrent;

        public PeriodSum(String label, double income, double expense, boolean isCurrent) {
            this.label = label;
            this.income = income;
            this.expense = expense;
            this.isCurrent = isCurrent;
        }
    }

    public StatsViewModel(Application application) {
        super(application);
        repository = MyApplication.getRepository();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getUserId();

        Calendar cal = Calendar.getInstance();
        statsYear.setValue(cal.get(Calendar.YEAR));
        statsMonth.setValue(cal.get(Calendar.MONTH));

        setPeriod(Period.MONTH);
    }

    // ==================== 基础 getter ====================

    public LiveData<Period> getPeriod() { return period; }
    public LiveData<Integer> getStatsYear() { return statsYear; }
    public LiveData<Integer> getStatsMonth() { return statsMonth; }
    public LiveData<Integer> getStatsWeekOffset() { return statsWeekOffset; }
    public LiveData<List<TypeSum>> getExpenseByType() { return expenseByType; }
    public LiveData<List<TypeSum>> getIncomeByType() { return incomeByType; }
    public LiveData<List<PeriodSum>> getTrendData() { return trendData; }

    // ==================== 周期切换 ====================

    public void goToPrevWeek() {
        Integer offset = statsWeekOffset.getValue();
        if (offset == null) offset = 0;
        statsWeekOffset.setValue(offset - 1);
        reloadAll();
    }

    public void goToNextWeek() {
        Integer offset = statsWeekOffset.getValue();
        if (offset == null) offset = 0;
        statsWeekOffset.setValue(offset + 1);
        reloadAll();
    }

    public void goToPrevMonth() {
        Integer m = statsMonth.getValue();
        Integer y = statsYear.getValue();
        if (m == null || y == null) return;
        if (m == 0) { statsMonth.setValue(11); statsYear.setValue(y - 1); }
        else { statsMonth.setValue(m - 1); }
        statsWeekOffset.setValue(0);
        reloadAll();
    }

    public void goToNextMonth() {
        Integer m = statsMonth.getValue();
        Integer y = statsYear.getValue();
        if (m == null || y == null) return;
        if (m == 11) { statsMonth.setValue(0); statsYear.setValue(y + 1); }
        else { statsMonth.setValue(m + 1); }
        statsWeekOffset.setValue(0);
        reloadAll();
    }

    public void goToPrevYear() {
        Integer y = statsYear.getValue();
        if (y == null) return;
        statsYear.setValue(y - 1);
        statsWeekOffset.setValue(0);
        reloadAll();
    }

    public void goToNextYear() {
        Integer y = statsYear.getValue();
        if (y == null) return;
        statsYear.setValue(y + 1);
        statsWeekOffset.setValue(0);
        reloadAll();
    }

    public void setPeriod(Period p) {
        Calendar cal = Calendar.getInstance();
        statsYear.setValue(cal.get(Calendar.YEAR));
        statsMonth.setValue(cal.get(Calendar.MONTH));
        statsWeekOffset.setValue(0);
        period.setValue(p);
        reloadAll();
    }

    // ==================== 标签文本 ====================

    public String getPeriodLabel() {
        Period p = period.getValue();
        Integer y = statsYear.getValue();
        Integer m = statsMonth.getValue();

        if (p == null) p = Period.MONTH;
        if (y == null) y = Calendar.getInstance().get(Calendar.YEAR);
        if (m == null) m = Calendar.getInstance().get(Calendar.MONTH);

        switch (p) {
            case YEAR:
                return y + "年";
            case MONTH:
                return y + "年" + (m + 1) + "月";
            case WEEK: {
                long weekStart = getWeekStart();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(weekStart);
                int actualY = cal.get(Calendar.YEAR);
                int actualM = cal.get(Calendar.MONTH) + 1;
                return actualY + "年" + actualM + "月  第" + getWeekOfMonth() + "周";
            }
        }
        return "";
    }

    public String getPeriodSubtitle() {
        Period p = period.getValue();
        if (p != Period.WEEK) return "";

        long start = getWeekStart();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        String from = (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DAY_OF_MONTH) + "日";
        cal.add(Calendar.DAY_OF_YEAR, 6);
        String to = (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DAY_OF_MONTH) + "日";
        return from + " - " + to;
    }

    private int getWeekOfMonth() {
        long weekStart = getWeekStart();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(weekStart);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        return (dayOfMonth - 1) / 7 + 1;
    }

    // ==================== 时间计算 ====================

    private long getWeekStart() {
        Integer offset = statsWeekOffset.getValue();
        if (offset == null) offset = 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // 从今天向前找到本周的周一（包含今天的那一周）
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.add(Calendar.DAY_OF_YEAR, offset * 7);
        return cal.getTimeInMillis();
    }

    private long getMonthStart() {
        Integer y = statsYear.getValue();
        Integer m = statsMonth.getValue();
        if (y == null) y = Calendar.getInstance().get(Calendar.YEAR);
        if (m == null) m = Calendar.getInstance().get(Calendar.MONTH);

        Calendar cal = Calendar.getInstance();
        cal.set(y, m, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getYearStart() {
        Integer y = statsYear.getValue();
        if (y == null) y = Calendar.getInstance().get(Calendar.YEAR);

        Calendar cal = Calendar.getInstance();
        cal.set(y, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // ==================== 数据加载 ====================

    private void reloadAll() {
        loadPieData();
        loadTrendData();
    }

    private void loadPieData() {
        Period p = period.getValue();
        if (p == null) p = Period.MONTH;
        long start, end;

        switch (p) {
            case WEEK:
                start = getWeekStart();
                end = addDays(start, 7);
                break;
            case MONTH:
                start = getMonthStart();
                end = addMonths(start, 1);
                break;
            case YEAR:
                start = getYearStart();
                end = addYears(start, 1);
                break;
            default:
                start = getMonthStart();
                end = addMonths(start, 1);
        }

        if (currentExpenseByTypeSource != null) {
            expenseByType.removeSource(currentExpenseByTypeSource);
            incomeByType.removeSource(currentIncomeByTypeSource);
        }

        currentExpenseByTypeSource = repository.getExpenseByType(userId, start, end);
        expenseByType.addSource(currentExpenseByTypeSource, new Observer<List<TypeSum>>() {
            @Override
            public void onChanged(List<TypeSum> value) {
                expenseByType.setValue(value);
            }
        });

        currentIncomeByTypeSource = repository.getIncomeByType(userId, start, end);
        incomeByType.addSource(currentIncomeByTypeSource, new Observer<List<TypeSum>>() {
            @Override
            public void onChanged(List<TypeSum> value) {
                incomeByType.setValue(value);
            }
        });
    }

    private void loadTrendData() {
        Period p = period.getValue();
        if (p == null) p = Period.MONTH;

        if (currentDailySource != null) {
            trendData.removeSource(currentDailySource);
        }
        currentDailySource = null;

        switch (p) {
            case WEEK:
                loadWeekTrend();
                break;
            case MONTH:
                loadMonthTrend();
                break;
            case YEAR:
                loadYearTrend();
                break;
        }
    }

    private void loadWeekTrend() {
        long start = getWeekStart();
        final long weekStart = start;

        currentDailySource = repository.getDailySum(userId, start, addDays(start, 7));
        trendData.addSource(currentDailySource, new Observer<List<DaySum>>() {
            @Override
            public void onChanged(List<DaySum> value) {
                List<PeriodSum> list = new ArrayList<>();
                if (value != null) {
                    for (DaySum ds : value) {
                        String label = getWeekDayLabel(weekStart, ds.day);
                        list.add(new PeriodSum(label, ds.income, ds.expense, false));
                    }
                }
                trendData.setValue(list);
            }
        });
    }

    private void loadMonthTrend() {
        long start = getMonthStart();

        currentDailySource = repository.getDailySum(userId, start, addMonths(start, 1));
        trendData.addSource(currentDailySource, new Observer<List<DaySum>>() {
            @Override
            public void onChanged(List<DaySum> value) {
                List<PeriodSum> list = new ArrayList<>();
                if (value != null) {
                    for (DaySum ds : value) {
                        list.add(new PeriodSum(ds.day + "日", ds.income, ds.expense, false));
                    }
                }
                trendData.setValue(list);
            }
        });
    }

    private void loadYearTrend() {
        Integer y = statsYear.getValue();
        if (y == null) y = Calendar.getInstance().get(Calendar.YEAR);
        final int year = y;

        // 清空旧源
        if (currentDailySource != null) {
            trendData.removeSource(currentDailySource);
        }

        List<LiveData<Double>> incomeSources = new ArrayList<>();
        List<LiveData<Double>> expenseSources = new ArrayList<>();
        final int[] loaded = {0};

        Observer<Double> observer = new Observer<Double>() {
            @Override
            public void onChanged(Double value) {
                loaded[0]++;
                if (loaded[0] >= 24) {
                    List<PeriodSum> result = new ArrayList<>();
                    for (int i = 0; i < 12; i++) {
                        Double inc = incomeSources.get(i).getValue();
                        Double exp = expenseSources.get(i).getValue();
                        result.add(new PeriodSum((i + 1) + "月",
                                inc != null ? inc : 0,
                                exp != null ? exp : 0,
                                false));
                    }
                    trendData.setValue(result);
                }
            }
        };

        for (int i = 0; i < 12; i++) {
            Calendar c = Calendar.getInstance();
            c.set(year, i, 1, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long ms = c.getTimeInMillis();
            c.add(Calendar.MONTH, 1);
            long me = c.getTimeInMillis();

            LiveData<Double> inc = repository.getIncomeBetween(userId, ms, me);
            LiveData<Double> exp = repository.getExpenseBetween(userId, ms, me);
            incomeSources.add(inc);
            expenseSources.add(exp);
            trendData.addSource(inc, observer);
            trendData.addSource(exp, observer);
        }
    }

    // ==================== 工具方法 ====================

    private long addDays(long time, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTimeInMillis();
    }

    private long addMonths(long time, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.MONTH, months);
        return cal.getTimeInMillis();
    }

    private long addYears(long time, int years) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.YEAR, years);
        return cal.getTimeInMillis();
    }

    private String getWeekDayLabel(long weekStart, String dayNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(weekStart);
        try {
            int day = Integer.parseInt(dayNum);
            int startDay = cal.get(Calendar.DAY_OF_MONTH);
            int diff = day - startDay;
            cal.add(Calendar.DAY_OF_YEAR, diff);
        } catch (Exception e) {
            // ignore
        }
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String[] days = {"", "日", "一", "二", "三", "四", "五", "六"};
        return "周" + days[dayOfWeek];
    }
}