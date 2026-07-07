package com.example.myapplication.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.dao.TypeSum;
import com.example.myapplication.ui.adapter.TrendAdapter;
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
    private RecyclerView recyclerTrend;
    private TrendAdapter trendAdapter;
    private StatsViewModel viewModel;

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
        recyclerTrend = view.findViewById(R.id.recycler_trend);

        tvPeriodLabel = view.findViewById(R.id.tv_period_label);
        tvPeriodSubtitle = view.findViewById(R.id.tv_period_subtitle);
        btnPeriodPrev = view.findViewById(R.id.btn_period_prev);
        btnPeriodNext = view.findViewById(R.id.btn_period_next);

        setupPieChart(pieExpenseChart);
        setupPieChart(pieIncomeChart);

        trendAdapter = new TrendAdapter();
        recyclerTrend.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTrend.setAdapter(trendAdapter);

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
        viewModel.getExpenseByType().observe(getViewLifecycleOwner(), typeSums -> updatePieChart(pieExpenseChart, typeSums));
        viewModel.getIncomeByType().observe(getViewLifecycleOwner(), typeSums -> updatePieChart(pieIncomeChart, typeSums));
        viewModel.getTrendData().observe(getViewLifecycleOwner(), periodSums -> trendAdapter.setData(periodSums));

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
        chart.setUsePercentValues(false); // manually compute percentages
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(40f);
        chart.setTransparentCircleRadius(45f);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(9f);
        chart.setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        chart.setDrawEntryLabels(true);
        chart.getLegend().setEnabled(false);
    }

    private void updatePieChart(PieChart chart, List<TypeSum> typeSums) {
        if (typeSums == null || typeSums.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        // Calculate total for manual percentage computation
        double total = 0;
        for (TypeSum ts : typeSums) {
            total += Math.abs(ts.total);
        }
        final double finalTotal = total;

        List<PieEntry> entries = new ArrayList<>();
        for (TypeSum ts : typeSums) {
            entries.add(new PieEntry((float) Math.abs(ts.total), ts.type));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(CAT_COLORS);
        dataSet.setValueTextSize(11f);
        int outsideColor = (chart == pieExpenseChart) ?
                getResources().getColor(R.color.expense, requireContext().getTheme()) :
                getResources().getColor(R.color.income, requireContext().getTheme());
        dataSet.setValueTextColor(outsideColor);
        // Category name inside slice, amount/percentage outside with connector lines
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setSliceSpace(2f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.6f);
        dataSet.setValueLineColor(Color.parseColor("#94a3b8"));
        dataSet.setValueLineVariableLength(true);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                float percentage = (value / (float) finalTotal) * 100;
                return String.format(java.util.Locale.CHINA, "%.2f%%\n¥%.0f", percentage, value);
            }
        });
        chart.setData(pieData);
        chart.invalidate();
    }
}