package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;

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
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
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
        // 标题显示分类名
        holder.title.setText(bill.getType());
        // 副标题显示日期，如有备注则附加
        String metaText = sdf.format(new Date(bill.getTimestamp()));
        if (bill.getNote() != null && !bill.getNote().isEmpty()) {
            metaText += " · " + bill.getNote();
        }
        holder.meta.setText(metaText);

        boolean isIncome = "income".equals(bill.getCategory());
        holder.amount.setText((isIncome ? "+" : "-") + "¥" + String.format(Locale.CHINA, "%.2f", Math.abs(bill.getAmount())));
        holder.amount.setTextColor(holder.itemView.getContext().getColor(isIncome ? R.color.income : R.color.expense));

        holder.icon.setText(getIconForType(bill.getType()));
        int bgColor = getBgColorForType(bill.getType());
        holder.icon.setBackgroundColor(holder.itemView.getContext().getColor(bgColor));

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
        if (type == null) return "💳";
        switch (type) {
            // 支出
            case "餐饮": return "🍔";
            case "购物": return "🛒";
            case "住房": return "🏠";
            case "交通": return "🚕";
            case "旅行": return "✈️";
            case "通讯": return "📱";
            case "娱乐": return "🎬";
            case "人情": return "🎁";
            case "医疗": return "💊";
            case "教育": return "📚";
            case "美容": return "💄";
            case "其他": return "📦";
            // 收入
            case "转账": return "💸";
            case "红包": return "🧧";
            case "退款": return "↩️";
            default: return "💳";
        }
    }

    private int getBgColorForType(String type) {
        if (type == null) return R.color.cat_other;
        switch (type) {
            // 支出
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
            // 收入
            case "转账": return R.color.cat_transfer;
            case "红包": return R.color.cat_redpacket;
            case "退款": return R.color.cat_refund;
            default: return R.color.cat_other;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, meta, amount;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.bill_icon);
            title = itemView.findViewById(R.id.bill_title);
            meta = itemView.findViewById(R.id.bill_meta);
            amount = itemView.findViewById(R.id.bill_amount);
        }
    }
}
