package com.example.autobookkeep.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final Context context;
    private List<String> categories;
    private String selectedCategory;
    private final OnCategoryClickListener listener;
    private OnCategoryLongClickListener longListener;
    private final boolean isDetailed;

    private final int selectedColor;
    private final int unselectedColor;
    private final int selectedTextColor;
    private final int unselectedTextColor;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public interface OnCategoryLongClickListener {
        boolean onCategoryLongClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, String currentCategory, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.selectedCategory = currentCategory;
        this.listener = listener;

        this.selectedColor = ContextCompat.getColor(context, R.color.app_blue);
        this.selectedTextColor = ContextCompat.getColor(context, R.color.cat_selected_text);
        this.unselectedColor = ContextCompat.getColor(context, R.color.cat_unselected_bg);
        this.unselectedTextColor = ContextCompat.getColor(context, R.color.cat_unselected_text);

        this.isDetailed = com.example.autobookkeep.util.CategoryManager.isSubCategoryEnabled(context);
    }

    public void setOnCategoryLongClickListener(OnCategoryLongClickListener longListener) {
        this.longListener = longListener;
    }

    public void updateData(List<String> newCategories) {
        this.categories = newCategories;
        if (!categories.contains(selectedCategory) && !categories.isEmpty()) {
            selectedCategory = categories.get(0);
            if (listener != null) listener.onCategoryClick(selectedCategory);
        }
        notifyDataSetChanged();
    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        notifyDataSetChanged();
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);

        if (isDetailed) {
            holder.tvIcon.setText(category);
            ViewGroup.LayoutParams lp = holder.tvIcon.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.tvIcon.setLayoutParams(lp);
            int paddingH = (int) (12 * context.getResources().getDisplayMetrics().density);
            int paddingV = (int) (6 * context.getResources().getDisplayMetrics().density);
            holder.tvIcon.setPadding(paddingH, paddingV, paddingH, paddingV);
            holder.tvIcon.setTextSize(14);
            holder.tvIcon.setTypeface(null, android.graphics.Typeface.NORMAL);
            background.setCornerRadius(50 * context.getResources().getDisplayMetrics().density);
        } else {
            if (category != null && !category.isEmpty()) {
                holder.tvIcon.setText(category);
            } else {
                holder.tvIcon.setText("");
            }
            ViewGroup.LayoutParams lp = holder.tvIcon.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = (int) (50 * context.getResources().getDisplayMetrics().density);
            holder.tvIcon.setLayoutParams(lp);
            int paddingH = (int) (12 * context.getResources().getDisplayMetrics().density);
            holder.tvIcon.setPadding(paddingH, 0, paddingH, 0);
            holder.tvIcon.setTextSize(18);
            holder.tvIcon.setTypeface(null, android.graphics.Typeface.BOLD);
            background.setCornerRadius(16 * context.getResources().getDisplayMetrics().density);
        }

        boolean isSelected = category.equals(selectedCategory);
        if (isSelected) {
            background.setColor(selectedColor);
            holder.tvIcon.setTextColor(selectedTextColor);
        } else {
            background.setColor(unselectedColor);
            holder.tvIcon.setTextColor(unselectedTextColor);
        }
        holder.tvIcon.setBackground(background);

        holder.itemView.setOnClickListener(v -> {
            selectedCategory = category;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                return longListener.onCategoryLongClick(category);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_category_icon);
        }
    }
}