package com.example.myapplication.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myapplication.ui.stats.StatsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrendBarView extends View {

    private List<StatsViewModel.PeriodSum> data = new ArrayList<>();
    private double maxValue = 1.0;

    private final Paint incomePaint = new Paint();
    private final Paint expensePaint = new Paint();
    private final Paint labelPaint = new Paint();
    private final Paint amountPaint = new Paint();
    private final Paint gridPaint = new Paint();

    private static final int INCOME_COLOR = 0xFF22C55E;
    private static final int EXPENSE_COLOR = 0xFFEF4444;
    private static final int LABEL_COLOR = 0xFF64748B;
    private static final int GRID_COLOR = 0xFFE5E7EB;

    public TrendBarView(Context context) {
        super(context);
        init();
    }

    public TrendBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrendBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        incomePaint.setColor(INCOME_COLOR);
        incomePaint.setStyle(Paint.Style.FILL);
        incomePaint.setAntiAlias(true);

        expensePaint.setColor(EXPENSE_COLOR);
        expensePaint.setStyle(Paint.Style.FILL);
        expensePaint.setAntiAlias(true);

        labelPaint.setColor(LABEL_COLOR);
        labelPaint.setTextSize(28f);
        labelPaint.setAntiAlias(true);

        amountPaint.setTextSize(24f);
        amountPaint.setAntiAlias(true);

        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setAntiAlias(true);
    }

    public void setData(List<StatsViewModel.PeriodSum> data) {
        this.data = data != null ? data : new ArrayList<>();
        double max = 1.0;
        for (StatsViewModel.PeriodSum ps : this.data) {
            if (ps.income > max) max = ps.income;
            if (ps.expense > max) max = ps.expense;
        }
        this.maxValue = max;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = 24;
        int paddingRight = 24;
        int paddingTop = 32;
        int paddingBottom = 48;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        int count = data.size();
        if (count == 0) return;

        float groupWidth = chartWidth / (float) count;
        float barWidth = Math.min(groupWidth * 0.35f, 32f);
        float barGap = Math.max(groupWidth * 0.1f, 8f);

        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = paddingTop + chartHeight * i / gridLines;
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
        }

        for (int i = 0; i < count; i++) {
            StatsViewModel.PeriodSum ps = data.get(i);

            float groupX = paddingLeft + groupWidth * i + groupWidth / 2;

            float incomeHeight = (float) (chartHeight * (ps.income / maxValue));
            float expenseHeight = (float) (chartHeight * (ps.expense / maxValue));

            if (incomeHeight < 4 && ps.income > 0) incomeHeight = 4;
            if (expenseHeight < 4 && ps.expense > 0) expenseHeight = 4;

            float incomeLeft = groupX - barWidth - barGap / 2;
            float incomeRight = groupX - barGap / 2;
            float incomeTop = paddingTop + chartHeight - incomeHeight;
            float incomeBottom = paddingTop + chartHeight;

            canvas.drawRect(incomeLeft, incomeTop, incomeRight, incomeBottom, incomePaint);

            float expenseLeft = groupX + barGap / 2;
            float expenseRight = groupX + barWidth + barGap / 2;
            float expenseTop = paddingTop + chartHeight - expenseHeight;
            float expenseBottom = paddingTop + chartHeight;

            canvas.drawRect(expenseLeft, expenseTop, expenseRight, expenseBottom, expensePaint);

            float labelX = groupX;
            float labelY = height - paddingBottom + 24;
            labelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(ps.label, labelX, labelY, labelPaint);

            if (groupWidth > 80) {
                String incomeText = String.format(Locale.CHINA, "%.0f", ps.income);
                String expenseText = String.format(Locale.CHINA, "%.0f", ps.expense);

                amountPaint.setColor(INCOME_COLOR);
                amountPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(incomeText, incomeLeft + barWidth / 2, incomeTop - 8, amountPaint);

                amountPaint.setColor(EXPENSE_COLOR);
                amountPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(expenseText, expenseLeft + barWidth / 2, expenseTop - 8, amountPaint);
            }
        }
    }
}