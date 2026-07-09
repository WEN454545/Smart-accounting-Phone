package com.example.myapplication.ui.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.dao.DaySum;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.ui.adapter.BillAdapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment implements BillAdapter.OnBillLongClickListener {

    private static final String PREF_NAME = "profile_settings";
    private static final String KEY_CAL_BG_URI = "cal_bg_uri";

    private TextView monthText, incomeText, expenseText;
    private TextView selectedDateText, selectedIncomeText, selectedExpenseText;
    private ImageButton btnPrev, btnNext;
    private LinearLayout calendarGrid;
    private ImageView ivCalendarBg;
    private LinearLayout selectedDayBar;
    private LinearLayout emptyHint;
    private RecyclerView rvDayBills;
    private CalendarViewModel viewModel;
    private BillAdapter billAdapter;
    private Integer selectedDayNum = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        monthText = view.findViewById(R.id.tv_month_year);
        incomeText = view.findViewById(R.id.tv_cal_income);
        expenseText = view.findViewById(R.id.tv_cal_expense);
        calendarGrid = view.findViewById(R.id.calendar_grid);
        ivCalendarBg = view.findViewById(R.id.iv_calendar_bg);
        btnPrev = view.findViewById(R.id.btn_prev_month);
        btnNext = view.findViewById(R.id.btn_next_month);
        selectedDayBar = view.findViewById(R.id.selected_day_bar);
        selectedDateText = view.findViewById(R.id.tv_selected_date);
        selectedIncomeText = view.findViewById(R.id.tv_selected_income);
        selectedExpenseText = view.findViewById(R.id.tv_selected_expense);
        rvDayBills = view.findViewById(R.id.rv_day_bills);
        emptyHint = view.findViewById(R.id.empty_hint);

        rvDayBills.setLayoutManager(new LinearLayoutManager(requireContext()));
        billAdapter = new BillAdapter(null);
        billAdapter.setOnLongClickListener(this);
        rvDayBills.setAdapter(billAdapter);

        btnPrev.setOnClickListener(v -> viewModel.goToPreviousMonth());
        btnNext.setOnClickListener(v -> viewModel.goToNextMonth());

        viewModel.getYear().observe(getViewLifecycleOwner(), y -> {
            renderCalendar();
        });
        viewModel.getMonth().observe(getViewLifecycleOwner(), m -> {
            viewModel.clearSelection();
            selectedDayNum = null;
            // 切换月份后，默认选中当天（如果切换到当前月份）
            Calendar now = Calendar.getInstance();
            Integer curY = viewModel.getYear().getValue();
            if (curY != null && curY.intValue() == now.get(Calendar.YEAR)
                    && m == now.get(Calendar.MONTH)) {
                int todayD = now.get(Calendar.DAY_OF_MONTH);
                selectedDayNum = todayD;
                viewModel.selectDay(todayD);
                updateSelectedDayLabel(curY, m, todayD);
            }
            renderCalendar();
        });

        viewModel.getIncome().observe(getViewLifecycleOwner(), income -> {
            double val = income != null ? income : 0;
            incomeText.setText("¥" + String.format(Locale.CHINA, "%,.2f", val));
        });
        viewModel.getExpense().observe(getViewLifecycleOwner(), expense -> {
            double val = expense != null ? expense : 0;
            expenseText.setText("¥" + String.format(Locale.CHINA, "%,.2f", val));
        });

        viewModel.getDailySums().observe(getViewLifecycleOwner(), sums -> renderCalendar());

        viewModel.getBillsForDay().observe(getViewLifecycleOwner(), bills -> {
            billAdapter.setBills(bills);
            updateSelectedDaySummary(bills);

            boolean hasBills = bills != null && !bills.isEmpty();
            boolean hasSelection = selectedDayNum != null;

            if (hasSelection) {
                selectedDayBar.setVisibility(View.VISIBLE);
                rvDayBills.setVisibility(View.VISIBLE);
                emptyHint.setVisibility(View.GONE);
            } else {
                selectedDayBar.setVisibility(View.GONE);
                rvDayBills.setVisibility(View.GONE);
                emptyHint.setVisibility(View.VISIBLE);
            }
        });

        loadCalendarBackground();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCalendarBackground();
        // Force re-query month data to pick up changes made on other tabs
        viewModel.reloadMonthData();
    }

    private void loadCalendarBackground() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_CAL_BG_URI, null);
        ivCalendarBg.setVisibility(View.VISIBLE);
        if (uriStr != null) {
            ivCalendarBg.setImageURI(Uri.parse(uriStr));
        } else {
            ivCalendarBg.setImageResource(R.drawable.bg_calendar_default);
        }
    }

    @Override
    public void onBillLongClick(Bill bill) {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setTitle("删除账单")
                .setMessage("确定要删除 " + bill.getType() + " ¥" + String.format(java.util.Locale.CHINA, "%.2f", Math.abs(bill.getAmount())) + " 吗？")
                .setPositiveButton("删除", (d, w) -> {
                    viewModel.deleteBill(bill);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void renderCalendar() {
        calendarGrid.removeAllViews();

        Integer year = viewModel.getYear().getValue();
        Integer month = viewModel.getMonth().getValue();
        if (year == null || month == null) return;

        monthText.setText(year + "年" + (month + 1) + "月");

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int startDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Map<Integer, DaySum> dayData = new HashMap<>();
        List<DaySum> sums = viewModel.getDailySums().getValue();
        if (sums != null) {
            for (DaySum ds : sums) {
                try { dayData.put(Integer.parseInt(ds.day), ds); }
                catch (NumberFormatException ignored) {}
            }
        }

        float density = getResources().getDisplayMetrics().density;
        int dp4 = (int) (4 * density);
        int dp8 = (int) (8 * density);

        int totalCells = startDay + daysInMonth;
        int numDataRows = (int) Math.ceil(totalCells / 7.0);
        int totalRows = 1 + numDataRows;

        calendarGrid.setWeightSum(totalRows);

        LinearLayout headerRow = createRow(1f);
        String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
        for (String day : weekDays) {
            TextView tv = createDayLabel(day);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            headerRow.addView(tv);
        }
        calendarGrid.addView(headerRow);

        Calendar today = Calendar.getInstance();
        boolean isCurrentMonth = (year.intValue() == today.get(Calendar.YEAR) && month.intValue() == today.get(Calendar.MONTH));
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        int cellIndex = 0;
        for (int row = 0; row < numDataRows; row++) {
            LinearLayout dayRow = createRow(1f);
            for (int col = 0; col < 7; col++) {
                int dayNum = cellIndex - startDay + 1;
                cellIndex++;

                if (dayNum < 1 || dayNum > daysInMonth) {
                    View empty = new View(requireContext());
                    empty.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    dayRow.addView(empty);
                } else {
                    DaySum daySum = dayData.get(dayNum);
                    boolean isSelected = selectedDayNum != null && selectedDayNum == dayNum;
                    LinearLayout cell = createDayCell(dayNum, daySum, isCurrentMonth && dayNum == todayDay, isSelected, year, month);
                    cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    dayRow.addView(cell);
                }
            }
            calendarGrid.addView(dayRow);
        }
    }

    private LinearLayout createRow(float weight) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, weight));
        return row;
    }

    private TextView createDayLabel(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(requireContext().getColor(R.color.muted));
        tv.setTextSize(12);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        float density = getResources().getDisplayMetrics().density;
        tv.setPadding(0, (int)(8*density), 0, (int)(8*density));
        return tv;
    }

    private LinearLayout createDayCell(int dayNum, DaySum daySum, boolean isToday, boolean isSelected, int year, int month) {
        float density = getResources().getDisplayMetrics().density;
        int dp4 = (int) (4 * density);

        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp4, dp4/2, dp4, dp4/2);

        boolean hasBg = ivCalendarBg.getDrawable() != null;
        int bgColorRes = hasBg ? R.drawable.bg_cal_cell_neutral_bg : R.drawable.bg_cal_cell_neutral;
        int textColorVal = requireContext().getColor(R.color.muted);
        double income = daySum != null ? daySum.income : 0;
        double expense = daySum != null ? daySum.expense : 0;
        double net = income - expense;

        if (isSelected) {
            bgColorRes = hasBg ? R.drawable.bg_cal_cell_selected_bg : R.drawable.bg_cal_cell_selected;
            textColorVal = Color.WHITE;
        } else {
            if (net > 0) {
                bgColorRes = hasBg ? R.drawable.bg_cal_cell_income_bg : R.drawable.bg_cal_cell_income;
                textColorVal = requireContext().getColor(R.color.income);
            } else if (net < 0) {
                bgColorRes = hasBg ? R.drawable.bg_cal_cell_expense_bg : R.drawable.bg_cal_cell_expense;
                textColorVal = requireContext().getColor(R.color.expense);
            }
        }

        cell.setBackgroundResource(bgColorRes);

        TextView dayNumTv = new TextView(requireContext());
        dayNumTv.setText(String.valueOf(dayNum));
        dayNumTv.setGravity(Gravity.CENTER);
        dayNumTv.setTextSize(16);
        dayNumTv.setTypeface(null, android.graphics.Typeface.BOLD);
        dayNumTv.setTextColor(textColorVal);

        if (isToday) {
            dayNumTv.setBackgroundResource(R.drawable.bg_today);
            dayNumTv.setPadding(dp4, dp4/2, dp4, dp4/2);
        }

        if (isSelected && isToday) {
            dayNumTv.setTextColor(Color.WHITE);
        }

        cell.addView(dayNumTv);

        if (net != 0) {
            TextView netText = new TextView(requireContext());
            netText.setGravity(Gravity.CENTER);
            int netColor = isSelected
                    ? (net > 0 ? requireContext().getColor(R.color.income) : requireContext().getColor(R.color.expense))
                    : textColorVal;
            netText.setTextColor(netColor);
            netText.setTextSize(9);
            netText.setSingleLine(true);
            netText.setEllipsize(android.text.TextUtils.TruncateAt.END);
            netText.setPadding(0, 0, 0, 0);
            netText.setText(formatNet(net));
            cell.addView(netText);
        }

        cell.setOnClickListener(v -> {
            if (selectedDayNum != null && selectedDayNum == dayNum) {
                selectedDayNum = null;
                viewModel.clearSelection();
            } else {
                selectedDayNum = dayNum;
                viewModel.selectDay(dayNum);
                updateSelectedDayLabel(year, month, dayNum);
            }
            renderCalendar();
        });

        cell.setClickable(true);
        cell.setFocusable(true);

        return cell;
    }

    private void updateSelectedDayLabel(int year, int month, int day) {
        selectedDateText.setText((month + 1) + "月" + day + "日账单");
    }

    private void updateSelectedDaySummary(List<Bill> bills) {
        double income = 0, expense = 0;
        if (bills != null) {
            for (Bill b : bills) {
                if ("income".equals(b.getCategory())) {
                    income += b.getAmount();
                } else {
                    expense += Math.abs(b.getAmount());
                }
            }
        }
        selectedIncomeText.setText(formatAmount(income, true));
        selectedExpenseText.setText(formatAmount(expense, false));
        selectedIncomeText.setVisibility(income > 0 ? View.VISIBLE : View.GONE);
        selectedExpenseText.setVisibility(expense > 0 ? View.VISIBLE : View.GONE);
    }

    private String formatNet(double net) {
        String sign = net > 0 ? "+" : "-";
        double abs = Math.abs(net);
        return sign + String.format(Locale.CHINA, "%,.2f", abs);
    }

    private String formatAmount(double amount, boolean isIncome) {
        String sign = isIncome ? "+" : "-";
        return sign + String.format(Locale.CHINA, "%,.2f", amount);
    }
}
