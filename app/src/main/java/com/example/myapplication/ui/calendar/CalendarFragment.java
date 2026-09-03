package com.example.myapplication.ui.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import com.example.myapplication.util.ColorSchemeManager;

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
            incomeText.setText("\u00a5" + String.format(Locale.CHINA, "%,.2f", val));
        });
        viewModel.getExpense().observe(getViewLifecycleOwner(), expense -> {
            double val = expense != null ? expense : 0;
            expenseText.setText("\u00a5" + String.format(Locale.CHINA, "%,.2f", val));
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
        viewModel.reloadMonthData();
        // Rebind bill icons: icon changes in settings only touch SharedPreferences,
        // which does not retrigger the bill LiveData.
        if (billAdapter != null) billAdapter.notifyDataSetChanged();
    }

    private void loadCalendarBackground() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedPath = prefs.getString(KEY_CAL_BG_URI, null);
        ivCalendarBg.setVisibility(View.VISIBLE);
        if (savedPath != null) {
            if (savedPath.startsWith("content://")) {
                ivCalendarBg.setImageURI(Uri.parse(savedPath));
            } else {
                ivCalendarBg.setImageURI(Uri.fromFile(new java.io.File(savedPath)));
            }
        } else {
            ivCalendarBg.setImageResource(R.drawable.bg_calendar_default);
        }
    }

    @Override
    public void onBillLongClick(Bill bill) {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setTitle("\u5220\u9664\u8d26\u5355")
                .setMessage("\u786e\u5b9a\u8981\u5220\u9664 " + bill.getType() + " \u00a5" + String.format(java.util.Locale.CHINA, "%.2f", Math.abs(bill.getAmount())) + " \u5417\uff1f")
                .setPositiveButton("\u5220\u9664", (d, w) -> {
                    viewModel.deleteBill(bill);
                })
                .setNegativeButton("\u53d6\u6d88", null)
                .show();
    }

    private void renderCalendar() {
        calendarGrid.removeAllViews();

        Integer year = viewModel.getYear().getValue();
        Integer month = viewModel.getMonth().getValue();
        if (year == null || month == null) return;

        monthText.setText(year + "\u5e74" + (month + 1) + "\u6708");

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
        String[] weekDays = {"\u65e5", "\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94", "\u516d"};
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
        if (isSelected) {
            int primaryColor = ColorSchemeManager.getCurrentPrimaryColor(requireContext());
            if (hasBg) {
                // Semi-transparent primary (80% opacity)
                GradientDrawable gd = new GradientDrawable();
                gd.setColor((primaryColor & 0x00FFFFFF) | 0xCC000000);
                gd.setCornerRadius(8 * density);
                cell.setBackground(gd);
            } else {
                cell.setBackgroundResource(R.drawable.bg_cal_cell_selected);
            }
        } else {
            int bgColorRes = hasBg ? R.drawable.bg_cal_cell_neutral_bg : R.drawable.bg_cal_cell_neutral;
            cell.setBackgroundResource(bgColorRes);
        }

        TextView dayNumTv = new TextView(requireContext());
        dayNumTv.setText(String.valueOf(dayNum));
        dayNumTv.setGravity(Gravity.CENTER);
        dayNumTv.setTextSize(16);
        dayNumTv.setTypeface(null, android.graphics.Typeface.BOLD);
        dayNumTv.setTextColor(isSelected ? Color.WHITE : requireContext().getColor(R.color.ink));

        if (isToday) {
            dayNumTv.setBackgroundResource(R.drawable.bg_today);
            dayNumTv.setPadding(dp4, dp4/2, dp4, dp4/2);
            if (!isSelected) {
                dayNumTv.setTextColor(ColorSchemeManager.getCurrentPrimaryColor(requireContext()));
            }
        }

        if (isSelected && isToday) {
            dayNumTv.setTextColor(Color.WHITE);
        }

        cell.addView(dayNumTv);

        // income amount
        double income = daySum != null ? daySum.income : 0;
        if (income > 0) {
            TextView incomeTv = new TextView(requireContext());
            incomeTv.setGravity(Gravity.CENTER);
            incomeTv.setTextColor(isSelected ? Color.WHITE : requireContext().getColor(R.color.income));
            incomeTv.setTextSize(8.5f);
            incomeTv.setSingleLine(true);
            incomeTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            incomeTv.setText("+" + String.format(Locale.CHINA, "%,.0f", income));
            cell.addView(incomeTv);
        }

        // expense amount
        double expense = daySum != null ? daySum.expense : 0;
        if (expense > 0) {
            TextView expenseTv = new TextView(requireContext());
            expenseTv.setGravity(Gravity.CENTER);
            expenseTv.setTextColor(isSelected ? Color.WHITE : requireContext().getColor(R.color.expense));
            expenseTv.setTextSize(8.5f);
            expenseTv.setSingleLine(true);
            expenseTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            expenseTv.setText("-" + String.format(Locale.CHINA, "%,.0f", expense));
            cell.addView(expenseTv);
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
        selectedDateText.setText((month + 1) + "\u6708" + day + "\u65e5\u8d26\u5355");
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

    private String formatAmount(double amount, boolean isIncome) {
        String sign = isIncome ? "+" : "-";
        return sign + String.format(Locale.CHINA, "%,.2f", amount);
    }
}