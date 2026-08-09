package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.util.CategoryIconHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    public interface OnBillClickListener {
        void onBillClick(Bill bill);
    }

    public interface OnBillLongClickListener {
        void onBillLongClick(Bill bill);
    }

    private List<Bill> bills = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM\u6708dd\u65E5 HH:mm", Locale.CHINA);
    private final OnBillClickListener listener;
    private OnBillLongClickListener longClickListener;

    public BillAdapter(OnBillClickListener listener) {
        this.listener = listener;
    }

    public void setOnLongClickListener(OnBillLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills != null ? bills : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Bill getBillAt(int position) {
        if (position >= 0 && position < bills.size()) return bills.get(position);
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = bills.get(position);
        // title = category name
        holder.title.setText(bill.getType());
        // subtitle: date, append note if exists
        String metaText = sdf.format(new Date(bill.getTimestamp()));
        if (bill.getNote() != null && !bill.getNote().isEmpty()) {
            metaText += " \u00B7 " + bill.getNote();
        }
        holder.meta.setText(metaText);

        boolean isIncome = "income".equals(bill.getCategory());
        holder.amount.setText((isIncome ? "+" : "-") + "\u00A5" + String.format(Locale.CHINA, "%.2f", Math.abs(bill.getAmount())));
        holder.amount.setTextColor(holder.itemView.getContext().getColor(isIncome ? R.color.income : R.color.expense));

        int bgColor = getBgColorForType(bill.getType());
        CategoryIconHelper.bindIcon(holder.icon, holder.iconImage, bill.getType(),
                getIconForType(bill.getType()), bgColor, holder.itemView.getContext());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBillClick(bill);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onBillLongClick(bill);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    private String getIconForType(String type) {
        if (type == null) return "\uD83D\uDCB3";
        switch (type) {
            // expense
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
            // income
            case "\u8F6C\u8D26": return "\uD83D\uDCB8";
            case "\u7EA2\u5305": return "\uD83E\uDDE7";
            case "\u9000\u6B3E": return "\u21A9\uFE0F";
            default: return "\uD83D\uDCB3";
        }
    }

    private int getBgColorForType(String type) {
        if (type == null) return R.color.cat_other;
        switch (type) {
            // expense
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
            // income
            case "\u8F6C\u8D26": return R.color.cat_transfer;
            case "\u7EA2\u5305": return R.color.cat_redpacket;
            case "\u9000\u6B3E": return R.color.cat_refund;
            default: return R.color.cat_other;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, meta, amount;
        ImageView iconImage;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.bill_icon);
            iconImage = itemView.findViewById(R.id.bill_icon_image);
            title = itemView.findViewById(R.id.bill_title);
            meta = itemView.findViewById(R.id.bill_meta);
            amount = itemView.findViewById(R.id.bill_amount);
        }
    }
}
