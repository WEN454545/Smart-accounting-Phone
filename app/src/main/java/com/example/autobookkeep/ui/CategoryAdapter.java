package com.example.autobookkeep.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.util.CategoryIconHelper;

import java.util.List;

/**
 * Adapter for the category grid in the auto-bookkeeping confirmation window.
 * Shows each category as a compact icon + label item.
 * Uses PNG icons from CategoryIconHelper when available, emoji fallback otherwise.
 */
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
    private final float density;

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
        this.density = context.getResources().getDisplayMetrics().density;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_window_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        boolean isSelected = category.equals(selectedCategory);

        // Set label text
        holder.tvLabel.setText(category);

        // Bind icon: PNG icon if available, otherwise emoji fallback
        int iconResId = CategoryIconHelper.getIconResId(category);
        int bgColorRes = getBgColorForCategory(category);
        int bgColor = ContextCompat.getColor(context, bgColorRes);

        float cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f, context.getResources().getDisplayMetrics());

        if (iconResId != 0) {
            // Show PNG icon
            holder.ivIcon.setImageResource(iconResId);
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.tvEmoji.setVisibility(View.GONE);
            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.RECTANGLE);
            iconBg.setCornerRadius(cornerRadius);
            iconBg.setColor(bgColor);
            holder.ivIcon.setBackground(iconBg);
        } else {
            // Show emoji fallback
            String emoji = getEmojiForCategory(category);
            holder.tvEmoji.setText(emoji);
            holder.tvEmoji.setVisibility(View.VISIBLE);
            holder.ivIcon.setVisibility(View.GONE);
            GradientDrawable emojiBg = new GradientDrawable();
            emojiBg.setShape(GradientDrawable.RECTANGLE);
            emojiBg.setCornerRadius(cornerRadius);
            emojiBg.setColor(bgColor);
            holder.tvEmoji.setBackground(emojiBg);
        }

        // Apply selected state to the container
        GradientDrawable containerBg = new GradientDrawable();
        containerBg.setShape(GradientDrawable.RECTANGLE);
        float containerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12f, context.getResources().getDisplayMetrics());
        containerBg.setCornerRadius(containerRadius);
        if (isSelected) {
            containerBg.setColor(selectedColor);
            holder.tvLabel.setTextColor(selectedTextColor);
        } else {
            containerBg.setColor(unselectedColor);
            holder.tvLabel.setTextColor(unselectedTextColor);
        }
        holder.itemView.setBackground(containerBg);

        // Apply compact padding
        int padH = (int) (6 * density);
        int padV = (int) (4 * density);
        holder.itemView.setPadding(padH, padV, padH, padV);

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

    /**
     * Returns the emoji string for a given category name (fallback when no PNG icon).
     */
    private String getEmojiForCategory(String type) {
        if (type == null) return "\uD83D\uDCB3";
        switch (type) {
            // expense
            case "\u9910\u996E": return "\uD83C\uDF54";       // food
            case "\u8D2D\u7269": return "\uD83D\uDED2";       // shopping
            case "\u4F4F\u623F": return "\uD83C\uDFE0";       // housing
            case "\u4EA4\u901A": return "\uD83D\uDE95";       // transport
            case "\u65C5\u884C": return "\u2708\uFE0F";       // travel
            case "\u901A\u8BAF": return "\uD83D\uDCF1";       // communication
            case "\u5A31\u4E50": return "\uD83C\uDFAC";       // entertainment
            case "\u4EBA\u60C5": return "\uD83C\uDF81";       // social
            case "\u533B\u7597": return "\uD83D\uDC8A";       // medical
            case "\u6559\u80B2": return "\uD83D\uDCDA";       // education
            case "\u7F8E\u5BB9": return "\uD83D\uDC84";       // beauty
            case "\u5176\u4ED6": return "\uD83D\uDCE6";       // other
            // income
            case "\u8F6C\u8D26": return "\uD83D\uDCB8";       // transfer
            case "\u7EA2\u5305": return "\uD83E\uDDE7";       // redpacket
            case "\u9000\u6B3E": return "\u21A9\uFE0F";       // refund
            case "\u4E8C\u624B\u4EA4\u6613": return "\uD83D\uDCC8"; // secondhand
            case "\u5176\u4ED6\u6536\u5165": return "\uD83D\uDCB0"; // other income
            default: return "\uD83D\uDCB3";
        }
    }

    /**
     * Returns the background color resource for a given category name.
     */
    private int getBgColorForCategory(String type) {
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
        FrameLayout iconContainer;
        TextView tvEmoji;
        ImageView ivIcon;
        TextView tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.fl_category_icon_container);
            tvEmoji = itemView.findViewById(R.id.tv_category_emoji);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvLabel = itemView.findViewById(R.id.tv_category_label);
        }
    }
}
