package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Groups bills by day (e.g. "7月1日 周三") with section headers.
 * Used on the home page for month-level bill display.
 */
public class DaySectionedBillAdapter extends RecyclerView.Adapter<DaySectionedBillAdapter.SectionViewHolder> {

    private final Map<String, List<Bill>> groupedBills = new LinkedHashMap<>();
    private final List<String> sectionTitles = new ArrayList<>();
    private final BillAdapter.OnBillClickListener listener;
    private BillAdapter.OnBillLongClickListener longClickListener;
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("M月d日", Locale.CHINA);
    private static final String[] WEEK_DAYS = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    public DaySectionedBillAdapter(BillAdapter.OnBillClickListener listener) {
        this.listener = listener;
    }

    public void setOnLongClickListener(BillAdapter.OnBillLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setBills(List<Bill> bills) {
        groupedBills.clear();
        sectionTitles.clear();

        if (bills != null && !bills.isEmpty()) {
            for (Bill bill : bills) {
                String dayKey = getDayKey(bill.getTimestamp());
                if (!groupedBills.containsKey(dayKey)) {
                    groupedBills.put(dayKey, new ArrayList<>());
                    sectionTitles.add(dayKey);
                }
                groupedBills.get(dayKey).add(bill);
            }
        }

        notifyDataSetChanged();
    }

    private String getDayKey(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String dateStr = dayFormat.format(new Date(timestamp));
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=周日
        return dateStr + " " + WEEK_DAYS[dayOfWeek];
    }

    /** Return the date part only (e.g. "7月1日") from a day key. */
    private String getDatePart(String dayKey) {
        int spaceIdx = dayKey.indexOf(' ');
        return spaceIdx > 0 ? dayKey.substring(0, spaceIdx) : dayKey;
    }

    /** Return the weekday part only (e.g. "周三") from a day key. */
    private String getWeekdayPart(String dayKey) {
        int spaceIdx = dayKey.indexOf(' ');
        return spaceIdx > 0 ? dayKey.substring(spaceIdx + 1) : "";
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        String title = sectionTitles.get(position);
        List<Bill> bills = groupedBills.get(title);

        // Build title with date in bold + weekday in lighter color
        String datePart = getDatePart(title);
        String weekdayPart = getWeekdayPart(title);
        android.text.SpannableString spannable = new android.text.SpannableString(datePart + " " + weekdayPart);
        spannable.setSpan(new android.text.style.ForegroundColorSpan(
                holder.itemView.getContext().getColor(R.color.ink)), 0, datePart.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new android.text.style.ForegroundColorSpan(
                holder.itemView.getContext().getColor(R.color.muted)),
                datePart.length() + 1, datePart.length() + 1 + weekdayPart.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.titleText.setText(spannable);

        // Compute daily income/expense summary
        double dayIncome = 0, dayExpense = 0;
        for (Bill b : bills) {
            if ("income".equals(b.getCategory())) {
                dayIncome += b.getAmount();
            } else {
                dayExpense += Math.abs(b.getAmount());
            }
        }

        String summary = "收 " + String.format(java.util.Locale.CHINA, "%.2f", dayIncome)
                + "  支 " + String.format(java.util.Locale.CHINA, "%.2f", dayExpense);

        android.text.SpannableString summarySpan = new android.text.SpannableString(summary);
        // 收 ... (绿色)
        int incomeIdx = summary.indexOf("收 ");
        int incomeEnd = summary.indexOf("  支 ");
        summarySpan.setSpan(new android.text.style.ForegroundColorSpan(
                holder.itemView.getContext().getColor(R.color.income)),
                incomeIdx, incomeEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 支 ... (红色)
        int expenseIdx = summary.indexOf("支 ");
        summarySpan.setSpan(new android.text.style.ForegroundColorSpan(
                holder.itemView.getContext().getColor(R.color.expense)),
                expenseIdx, summary.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.summaryText.setText(summarySpan);

        DayBillAdapter adapter = new DayBillAdapter(bills, listener, longClickListener);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return sectionTitles.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView summaryText;
        RecyclerView recyclerView;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_section_title);
            summaryText = itemView.findViewById(R.id.tv_section_summary);
            recyclerView = itemView.findViewById(R.id.recycler_section_bills);
        }
    }

    /** Inner adapter for bills within a single day section. */
    private static class DayBillAdapter extends RecyclerView.Adapter<DayBillAdapter.ViewHolder> {
        private final List<Bill> bills;
        private final BillAdapter.OnBillClickListener listener;
        private final BillAdapter.OnBillLongClickListener longClickListener;

        DayBillAdapter(List<Bill> bills, BillAdapter.OnBillClickListener listener,
                       BillAdapter.OnBillLongClickListener longClickListener) {
            this.bills = bills != null ? bills : new ArrayList<>();
            this.listener = listener;
            this.longClickListener = longClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bill, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Bill bill = bills.get(position);
            holder.bind(bill, listener, longClickListener);
        }

        @Override
        public int getItemCount() {
            return bills.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView iconText, titleText, amountText, metaText;
            private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.bill_icon);
                titleText = itemView.findViewById(R.id.bill_title);
                amountText = itemView.findViewById(R.id.bill_amount);
                metaText = itemView.findViewById(R.id.bill_meta);
            }

            void bind(Bill bill, BillAdapter.OnBillClickListener listener,
                           BillAdapter.OnBillLongClickListener longClickListener) {
                String emoji = getIconForType(bill.getType());
                iconText.setText(emoji);
                int bgColor = getBgColorForType(bill.getType());
                iconText.setBackgroundColor(itemView.getContext().getColor(bgColor));

                titleText.setText(bill.getType());

                boolean isIncome = "income".equals(bill.getCategory());
                double amount = Math.abs(bill.getAmount());
                String amountStr = String.format(Locale.CHINA, "¥%,.2f", amount);
                amountText.setText((isIncome ? "+" : "-") + amountStr);
                amountText.setTextColor(itemView.getContext().getColor(
                        isIncome ? R.color.income : R.color.expense));

                // Show time only (date is in the section header)
                String metaStr = sdf.format(new Date(bill.getTimestamp()));
                if (bill.getNote() != null && !bill.getNote().isEmpty()) {
                    metaStr += " · " + bill.getNote();
                }
                metaText.setText(metaStr);

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onBillClick(bill);
                });
                itemView.setOnLongClickListener(v -> {
                    if (longClickListener != null) longClickListener.onBillLongClick(bill);
                    return true;
                });
            }

            private String getIconForType(String type) {
                if (type == null) return "\uD83D\uDCB3";
                switch (type) {
                    case "餐饮": return "\uD83C\uDF54";
                    case "购物": return "\uD83D\uDED2";
                    case "住房": return "\uD83C\uDFE0";
                    case "交通": return "\uD83D\uDE95";
                    case "旅行": return "\u2708\uFE0F";
                    case "通讯": return "\uD83D\uDCF1";
                    case "娱乐": return "\uD83C\uDFAC";
                    case "人情": return "\uD83C\uDF81";
                    case "医疗": return "\uD83D\uDC8A";
                    case "教育": return "\uD83D\uDCDA";
                    case "美容": return "\uD83D\uDC84";
                    case "其他": return "\uD83D\uDCE6";
                    case "转账": return "\uD83D\uDCB8";
                    case "红包": return "\uD83E\uDDE7";
                    case "退款": return "\u21A9\uFE0F";
                    default: return "\uD83D\uDCB3";
                }
            }

            private int getBgColorForType(String type) {
                if (type == null) return R.color.cat_other;
                switch (type) {
                    case "餐饮": return R.color.cat_food;
                    case "购物": return R.color.cat_shopping;
                    case "住房": return R.color.cat_housing;
                    case "交通": return R.color.cat_transport;
                    case "旅行": return R.color.cat_travel;
                    case "通讯": return R.color.cat_communication;
                    case "娱乐": return R.color.cat_entertainment;
                    case "人情": return R.color.cat_social;
                    case "医疗": return R.color.cat_medical;
                    case "教育": return R.color.cat_education;
                    case "美容": return R.color.cat_beauty;
                    case "其他": return R.color.cat_other;
                    case "转账": return R.color.cat_transfer;
                    case "红包": return R.color.cat_redpacket;
                    case "退款": return R.color.cat_refund;
                    default: return R.color.cat_other;
                }
            }
        }
    }
}