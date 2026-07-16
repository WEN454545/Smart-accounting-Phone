package com.example.myapplication.ui.stats;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.data.dao.TypeSum;
import com.example.myapplication.ui.view.TrendBarView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private PieChart pieExpenseChart;
    private PieChart pieIncomeChart;
    private TrendBarView trendView;
    private StatsViewModel viewModel;
    private LinearLayout legendExpense, legendIncome;

    private TextView tvPeriodLabel, tvPeriodSubtitle, btnPeriodPrev, btnPeriodNext;

    private final int[] CAT_COLORS = {
            Color.parseColor("#ef4444"),
            Color.parseColor("#0d9488"),
            Color.parseColor("#f59e0b"),
            Color.parseColor("#3b82f6"),
            Color.parseColor("#8b5cf6"),
            Color.parseColor("#ec4899"),
            Color.parseColor("#14b8a6"),
            Color.parseColor("#f97316"),
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        pieExpenseChart = view.findViewById(R.id.chart_pie_expense);
        pieIncomeChart = view.findViewById(R.id.chart_pie_income);
        trendView = view.findViewById(R.id.trend_view);
        legendExpense = view.findViewById(R.id.legend_expense);
        legendIncome = view.findViewById(R.id.legend_income);

        tvPeriodLabel = view.findViewById(R.id.tv_period_label);
        tvPeriodSubtitle = view.findViewById(R.id.tv_period_subtitle);
        btnPeriodPrev = view.findViewById(R.id.btn_period_prev);
        btnPeriodNext = view.findViewById(R.id.btn_period_next);

        setupPieChart(pieExpenseChart);
        setupPieChart(pieIncomeChart);

        // Tab 切换
        TextView tabWeek = view.findViewById(R.id.tab_week);
        TextView tabMonth = view.findViewById(R.id.tab_month);
        TextView tabYear = view.findViewById(R.id.tab_year);

        tabWeek.setOnClickListener(v -> selectTab(StatsViewModel.Period.WEEK, tabWeek, tabMonth, tabYear));
        tabMonth.setOnClickListener(v -> selectTab(StatsViewModel.Period.MONTH, tabWeek, tabMonth, tabYear));
        tabYear.setOnClickListener(v -> selectTab(StatsViewModel.Period.YEAR, tabWeek, tabMonth, tabYear));

        // 左右箭头切换
        btnPeriodPrev.setOnClickListener(v -> {
            StatsViewModel.Period p = viewModel.getPeriod().getValue();
            if (p == null) return;
            switch (p) {
                case WEEK: viewModel.goToPrevWeek(); break;
                case MONTH: viewModel.goToPrevMonth(); break;
                case YEAR: viewModel.goToPrevYear(); break;
            }
        });
        btnPeriodNext.setOnClickListener(v -> {
            StatsViewModel.Period p = viewModel.getPeriod().getValue();
            if (p == null) return;
            switch (p) {
                case WEEK: viewModel.goToNextWeek(); break;
                case MONTH: viewModel.goToNextMonth(); break;
                case YEAR: viewModel.goToNextYear(); break;
            }
        });

        // 数据观察
        viewModel.getExpenseByType().observe(getViewLifecycleOwner(), typeSums -> updatePieChart(pieExpenseChart, typeSums, legendExpense));
        viewModel.getIncomeByType().observe(getViewLifecycleOwner(), typeSums -> updatePieChart(pieIncomeChart, typeSums, legendIncome));
        viewModel.getTrendData().observe(getViewLifecycleOwner(), periodSums -> trendView.setData(periodSums));

        // 周期标签更新
        viewModel.getStatsYear().observe(getViewLifecycleOwner(), y -> updatePeriodLabel());
        viewModel.getStatsMonth().observe(getViewLifecycleOwner(), m -> updatePeriodLabel());
        viewModel.getStatsWeekOffset().observe(getViewLifecycleOwner(), o -> updatePeriodLabel());
        viewModel.getPeriod().observe(getViewLifecycleOwner(), p -> {
            updatePeriodLabel();
            updateTabStyle(p, tabWeek, tabMonth, tabYear);
        });

        selectTab(StatsViewModel.Period.WEEK, tabWeek, tabMonth, tabYear);

        return view;
    }

    private void updatePeriodLabel() {
        tvPeriodLabel.setText(viewModel.getPeriodLabel());
        String subtitle = viewModel.getPeriodSubtitle();
        if (subtitle.isEmpty()) {
            tvPeriodSubtitle.setVisibility(View.GONE);
        } else {
            tvPeriodSubtitle.setText(subtitle);
            tvPeriodSubtitle.setVisibility(View.VISIBLE);
        }
    }

    private void selectTab(StatsViewModel.Period p, TextView week, TextView month, TextView year) {
        viewModel.setPeriod(p);
    }

    private void updateTabStyle(StatsViewModel.Period p, TextView week, TextView month, TextView year) {
        int white = requireContext().getColor(android.R.color.white);
        int muted = requireContext().getColor(R.color.muted);

        week.setTextColor(p == StatsViewModel.Period.WEEK ? white : muted);
        week.setBackgroundResource(p == StatsViewModel.Period.WEEK ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        week.setTypeface(null, p == StatsViewModel.Period.WEEK ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        month.setTextColor(p == StatsViewModel.Period.MONTH ? white : muted);
        month.setBackgroundResource(p == StatsViewModel.Period.MONTH ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        month.setTypeface(null, p == StatsViewModel.Period.MONTH ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        year.setTextColor(p == StatsViewModel.Period.YEAR ? white : muted);
        year.setBackgroundResource(p == StatsViewModel.Period.YEAR ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        year.setTypeface(null, p == StatsViewModel.Period.YEAR ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void setupPieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(42f);
        chart.setTransparentCircleRadius(48f);
        chart.setExtraOffsets(8f, 5f, 8f, 5f);
        chart.setEntryLabelColor(Color.parseColor("#1e293b"));
        chart.setEntryLabelTextSize(10f);
        chart.setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        chart.setDrawEntryLabels(true);
        chart.getLegend().setEnabled(false);
    }

    private void updatePieChart(PieChart chart, List<TypeSum> typeSums, LinearLayout legendContainer) {
        if (typeSums == null || typeSums.isEmpty()) {
            chart.clear();
            chart.invalidate();
            legendContainer.removeAllViews();
            legendContainer.setVisibility(View.GONE);
            return;
        }

        // Calculate total
        double total = 0;
        for (TypeSum ts : typeSums) {
            total += Math.abs(ts.total);
        }
        final double finalTotal = total;

        // Split entries: major (>=10%) get labels inside, minor (<10%) use legend
        List<PieEntry> entries = new ArrayList<>();
        List<LegendItem> legendItems = new ArrayList<>();

        for (int i = 0; i < typeSums.size(); i++) {
            TypeSum ts = typeSums.get(i);
            float value = (float) Math.abs(ts.total);
            double percentage = (value / (float) finalTotal) * 100;
            int colorIndex = entries.size();

            if (percentage >= 10.0) {
                // Major category: label inside the chart
                entries.add(new PieEntry(value, ts.type));
            } else {
                // Minor category: no label on chart, use legend
                entries.add(new PieEntry(value, ""));
                legendItems.add(new LegendItem(ts.type, percentage, value, CAT_COLORS[colorIndex % CAT_COLORS.length]));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(CAT_COLORS);
        dataSet.setValueTextSize(10f);
        int accentColor = (chart == pieExpenseChart) ?
                getResources().getColor(R.color.expense, requireContext().getTheme()) :
                getResources().getColor(R.color.income, requireContext().getTheme());
        dataSet.setValueTextColor(accentColor);
        // Labels inside the slice
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                float percentage = (value / (float) finalTotal) * 100;
                if (percentage < 10.0f) {
                    return "";
                }
                return String.format(java.util.Locale.CHINA, "%.1f%%", percentage);
            }
        });
        chart.setData(pieData);
        chart.invalidate();

        // Build legend for minor categories
        buildLegend(legendContainer, legendItems);
    }

    /**
     * Build a custom legend view for minor categories (<10%).
     * Each item shows a colored dot, category name, and percentage.
     */
    private void buildLegend(LinearLayout container, List<LegendItem> items) {
        container.removeAllViews();
        if (items.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        int dotSize = (int) (8 * getResources().getDisplayMetrics().density);
        int marginBetween = (int) (6 * getResources().getDisplayMetrics().density);
        int marginRow = (int) (4 * getResources().getDisplayMetrics().density);
        int itemsPerRow = 3;

        LinearLayout currentRow = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % itemsPerRow == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) {
                    rowParams.topMargin = marginRow;
                }
                currentRow.setLayoutParams(rowParams);
                container.addView(currentRow);
            }

            LegendItem item = items.get(i);
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            if (i % itemsPerRow != 0) {
                itemParams.leftMargin = marginBetween;
            }
            itemLayout.setLayoutParams(itemParams);

            // Color dot
            View dot = new View(getContext());
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(item.color);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
            dot.setLayoutParams(dotParams);
            dot.setBackground(dotBg);
            itemLayout.addView(dot);

            // Category name + percentage
            TextView label = new TextView(getContext());
            label.setText(String.format(java.util.Locale.CHINA, " %s %.1f%%", item.name, item.percentage));
            label.setTextColor(Color.parseColor("#64748b"));
            label.setTextSize(11f);
            label.setMaxLines(1);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);
            itemLayout.addView(label);

            currentRow.addView(itemLayout);
        }
    }

    /**
     * Data class for legend items representing minor categories.
     */
    private static class LegendItem {
        final String name;
        final double percentage;
        final float value;
        final int color;

        LegendItem(String name, double percentage, float value, int color) {
            this.name = name;
            this.percentage = percentage;
            this.value = value;
            this.color = color;
        }
    }
}