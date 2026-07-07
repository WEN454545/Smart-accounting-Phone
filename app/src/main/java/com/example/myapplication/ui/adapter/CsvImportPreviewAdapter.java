package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvImportPreviewAdapter extends RecyclerView.Adapter<CsvImportPreviewAdapter.ViewHolder> {

    private final List<Bill> bills;
    private final boolean[] selected;

    public CsvImportPreviewAdapter(List<Bill> bills) {
        this.bills = bills;
        this.selected = new boolean[bills.size()];
        for (int i = 0; i < bills.size(); i++) {
            selected[i] = true; // default all selected
        }
    }

    public boolean[] getSelected() {
        return selected;
    }

    public void selectAll(boolean all) {
        for (int i = 0; i < selected.length; i++) {
            selected[i] = all;
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        int count = 0;
        for (boolean s : selected) {
            if (s) count++;
        }
        return count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_csv_import, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = bills.get(position);
        boolean isIncome = "income".equals(bill.getCategory());

        String typeLabel = isIncome ? "收入" : "支出";
        String amountColor = isIncome ? "#22c55e" : "#ef4444";
        holder.tvTypeAmount.setText(String.format(Locale.CHINA, "%s ¥%.2f", typeLabel, Math.abs(bill.getAmount())));
        holder.tvTypeAmount.setTextColor(android.graphics.Color.parseColor(amountColor));

        String note = bill.getNote() != null && !bill.getNote().isEmpty() ? " · " + bill.getNote() : "";
        holder.tvCategoryNote.setText(bill.getType() + note);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        holder.tvDate.setText(sdf.format(new Date(bill.getTimestamp())));

        holder.cbSelect.setChecked(selected[position]);
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selected[position] = isChecked;
            if (listener != null) listener.onSelectionChanged();
        });

        holder.itemView.setOnClickListener(v -> {
            holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    public List<Bill> getBills() {
        return bills;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private OnSelectionChangedListener listener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvTypeAmount, tvCategoryNote, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            tvTypeAmount = itemView.findViewById(R.id.tv_type_amount);
            tvCategoryNote = itemView.findViewById(R.id.tv_category_note);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}