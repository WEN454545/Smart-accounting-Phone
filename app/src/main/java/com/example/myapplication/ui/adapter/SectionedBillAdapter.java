package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.util.CategoryIconHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SectionedBillAdapter extends RecyclerView.Adapter<SectionedBillAdapter.SectionViewHolder> {

    private final Map<String, List<Bill>> groupedBills = new LinkedHashMap<>();
    private final List<String> sectionTitles = new ArrayList<>();
    private final BillAdapter.OnBillClickListener listener;
    private BillAdapter.OnBillLongClickListener longClickListener;

    public SectionedBillAdapter(BillAdapter.OnBillClickListener listener) {
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
                String yearMonth = getYearMonth(bill.getTimestamp());
                if (!groupedBills.containsKey(yearMonth)) {
                    groupedBills.put(yearMonth, new ArrayList<>());
                    sectionTitles.add(yearMonth);
                }
                groupedBills.get(yearMonth).add(bill);
            }
        }

        notifyDataSetChanged();
    }

    private String getYearMonth(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return year + "\u5E74" + month + "\u6708";
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
        holder.titleText.setText(title);

        List<Bill> bills = groupedBills.get(title);
        SectionBillAdapter adapter = new SectionBillAdapter(bills, listener, longClickListener);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return sectionTitles.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        RecyclerView recyclerView;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_section_title);
            recyclerView = itemView.findViewById(R.id.recycler_section_bills);
        }
    }

    // Inner adapter for bills within a section
    private static class SectionBillAdapter extends RecyclerView.Adapter<SectionBillAdapter.ViewHolder> {
        private final List<Bill> bills;
        private final BillAdapter.OnBillClickListener listener;
        private final BillAdapter.OnBillLongClickListener longClickListener;

        SectionBillAdapter(List<Bill> bills, BillAdapter.OnBillClickListener listener,
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
            private final ImageView iconImage;
            private final SimpleDateFormat sdf = new SimpleDateFormat("MM\u6708dd\u65E5 HH:mm", Locale.CHINA);

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.bill_icon);
                iconImage = itemView.findViewById(R.id.bill_icon_image);
                titleText = itemView.findViewById(R.id.bill_title);
                amountText = itemView.findViewById(R.id.bill_amount);
                metaText = itemView.findViewById(R.id.bill_meta);
            }

            void bind(Bill bill, BillAdapter.OnBillClickListener listener,
                           BillAdapter.OnBillLongClickListener longClickListener) {
                String emoji = getIconForType(bill.getType());
                int bgColor = getBgColorForType(bill.getType());
                CategoryIconHelper.bindIcon(iconText, iconImage, bill.getType(), emoji, bgColor, itemView.getContext());

                // title = category name
                titleText.setText(bill.getType());

                boolean isIncome = "income".equals(bill.getCategory());
                double amount = Math.abs(bill.getAmount());
                String amountStr = String.format(Locale.CHINA, "\u00A5%,.2f", amount);
                amountText.setText((isIncome ? "+" : "-") + amountStr);
                amountText.setTextColor(itemView.getContext().getColor(
                        isIncome ? R.color.income : R.color.expense));

                // subtitle: date, append note if exists
                String metaStr = sdf.format(new Date(bill.getTimestamp()));
                if (bill.getNote() != null && !bill.getNote().isEmpty()) {
                    metaStr += " \u00B7 " + bill.getNote();
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
                    case "\u9910\u996E": return "\uD83C\uDF54";
                    case "\u8D2D\u7269": return "\uD83D\uDED2";
                    case "\u4F4F\u623F": return "\uD83C\uDFE0";
                    case "\u4EA4\u901A": return "\uD83D\uDE95";
                    case "\u65C5\u884C": return "\u2708\uFE0F";
                    case "\u901A\u8BAF": return "\uD83D\uDCF1";
                    case "\u5A31\u4E50": return "\uD83C\uDFAC";
                    case "\u4EBA\u60C5": return "\uD83C\uDF81";
                    case "\u533B\u7597": return "\uD83D\uDC8A";
                    case "\u6559\u80B2": return "\uD83D\uDCDA";
                    case "\u7F8E\u5BB9": return "\uD83D\uDC84";
                    case "\u5176\u4ED6": return "\uD83D\uDCE6";
                    case "\u8F6C\u8D26": return "\uD83D\uDCB8";
                    case "\u7EA2\u5305": return "\uD83E\uDDE7";
                    case "\u9000\u6B3E": return "\u21A9\uFE0F";
                    default: return "\uD83D\uDCB3";
                }
            }

            private int getBgColorForType(String type) {
                if (type == null) return R.color.cat_other;
                switch (type) {
                    case "\u9910\u996E": return R.color.cat_food;
                    case "\u8D2D\u7269": return R.color.cat_shopping;
                    case "\u4F4F\u623F": return R.color.cat_housing;
                    case "\u4EA4\u901A": return R.color.cat_transport;
                    case "\u65C5\u884C": return R.color.cat_travel;
                    case "\u901A\u8BAF": return R.color.cat_communication;
                    case "\u5A31\u4E50": return R.color.cat_entertainment;
                    case "\u4EBA\u60C5": return R.color.cat_social;
                    case "\u533B\u7597": return R.color.cat_medical;
                    case "\u6559\u80B2": return R.color.cat_education;
                    case "\u7F8E\u5BB9": return R.color.cat_beauty;
                    case "\u5176\u4ED6": return R.color.cat_other;
                    case "\u8F6C\u8D26": return R.color.cat_transfer;
                    case "\u7EA2\u5305": return R.color.cat_redpacket;
                    case "\u9000\u6B3E": return R.color.cat_refund;
                    default: return R.color.cat_other;
                }
            }
        }
    }
}
