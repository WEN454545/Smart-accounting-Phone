package com.example.autobookkeep.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.util.CategoryIconHelper;
import com.example.autobookkeep.util.CategoryManager;
import com.example.autobookkeep.util.CategoryManager.IconInfo;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category presets page - manage expense/income categories
 */
public class CategorySettingsActivity extends AppCompatActivity {

    private RecyclerView rvExpense, rvIncome;
    private CategorySettingsAdapter expenseAdapter, incomeAdapter;
    private List<String> expenseList, incomeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_settings);

        CategoryManager.initDefaults(this);
        expenseList = CategoryManager.getExpenseCategories(this);
        incomeList = CategoryManager.getIncomeCategories(this);

        rvExpense = findViewById(R.id.rv_expense);
        rvIncome = findViewById(R.id.rv_income);

        expenseAdapter = new CategorySettingsAdapter(this, expenseList,
                (name, position) -> showEditDialog(name, true, position),
                (name, position) -> showDeleteDialog(name, true, position),
                (name, position) -> showIconPicker(name, true, position));
        incomeAdapter = new CategorySettingsAdapter(this, incomeList,
                (name, position) -> showEditDialog(name, false, position),
                (name, position) -> showDeleteDialog(name, false, position),
                (name, position) -> showIconPicker(name, false, position));

        rvExpense.setLayoutManager(new LinearLayoutManager(this));
        rvExpense.setAdapter(expenseAdapter);
        rvIncome.setLayoutManager(new LinearLayoutManager(this));
        rvIncome.setAdapter(incomeAdapter);

        findViewById(R.id.btn_add_expense).setOnClickListener(v -> showAddDialog(true));
        findViewById(R.id.btn_add_income).setOnClickListener(v -> showAddDialog(false));
    }

    private void showAddDialog(boolean isExpense) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_edit, null);
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText input = dialogView.findViewById(R.id.et_category_name);

        tvTitle.setText(isExpense ? "\u6DFB\u52A0\u652F\u51FA\u5206\u7C7B" : "\u6DFB\u52A0\u6536\u5165\u5206\u7C7B");
        input.setHint("\u8F93\u5165\u5206\u7C7B\u540D\u79F0");

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "\u540D\u79F0\u4E0D\u80FD\u4E3A\u7A7A", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> list = isExpense ? expenseList : incomeList;
            if (list.contains(name)) {
                Toast.makeText(this, "\u5206\u7C7B\u5DF2\u5B58\u5728", Toast.LENGTH_SHORT).show();
                return;
            }
            list.add(name);
            saveAndRefresh(isExpense);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditDialog(String oldName, boolean isExpense, int position) {
        if ("\u81EA\u5B9A\u4E49".equals(oldName)) {
            Toast.makeText(this, "\"\u81EA\u5B9A\u4E49\"\u662F\u4FDD\u7559\u5206\u7C7B\uFF0C\u4E0D\u53EF\u7F16\u8F91", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_edit, null);
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText input = dialogView.findViewById(R.id.et_category_name);

        tvTitle.setText("\u7F16\u8F91\u5206\u7C7B\u540D\u79F0");
        input.setText(oldName);
        input.setSelection(oldName.length());

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "\u540D\u79F0\u4E0D\u80FD\u4E3A\u7A7A", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> list = isExpense ? expenseList : incomeList;
            if (!newName.equals(oldName) && list.contains(newName)) {
                Toast.makeText(this, "\u5206\u7C7B\u5DF2\u5B58\u5728", Toast.LENGTH_SHORT).show();
                return;
            }
            list.set(position, newName);
            // Sync: migrate custom icon mapping and rename the category on all historical bills
            CategoryManager.renameCategoryIcon(this, oldName, newName);
            com.example.myapplication.MyApplication.getRepository().renameCategory(oldName, newName);
            // Keep the auto-bookkeeping database in sync so category learning
            // does not keep recommending the old (renamed) category name
            if (!newName.equals(oldName)) {
                com.example.autobookkeep.database.AppDatabase.databaseWriteExecutor.execute(() ->
                        com.example.autobookkeep.database.AppDatabase.getDatabase(this)
                                .transactionDao().renameCategory(oldName, newName));
            }
            saveAndRefresh(isExpense);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteDialog(String name, boolean isExpense, int position) {
        if ("\u81EA\u5B9A\u4E49".equals(name)) {
            Toast.makeText(this, "\"\u81EA\u5B9A\u4E49\"\u662F\u4FDD\u7559\u5206\u7C7B\uFF0C\u4E0D\u53EF\u5220\u9664", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_category, null);
        TextView tvCategoryName = dialogView.findViewById(R.id.tv_category_name);
        tvCategoryName.setText(name);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            List<String> list = isExpense ? expenseList : incomeList;
            list.remove(position);
            // Clean up the orphaned custom icon mapping
            CategoryManager.setCategoryIcon(this, name, null);
            saveAndRefresh(isExpense);
            dialog.dismiss();
            Toast.makeText(this, "\u5DF2\u5220\u9664", Toast.LENGTH_SHORT).show();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveAndRefresh(boolean isExpense) {
        if (isExpense) {
            CategoryManager.saveExpenseCategories(this, expenseList);
            Collections.sort(expenseList);
            expenseAdapter.notifyDataSetChanged();
        } else {
            CategoryManager.saveIncomeCategories(this, incomeList);
            Collections.sort(incomeList);
            incomeAdapter.notifyDataSetChanged();
        }
    }

    private void showIconPicker(String categoryName, boolean isExpense, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_icon_picker, null);
        com.google.android.flexbox.FlexboxLayout flexbox = dialogView.findViewById(R.id.flexbox_icons);

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.ThemeOverlay_RoundedBottomSheet);
        dialog.setContentView(dialogView);

        List<IconInfo> icons = CategoryManager.getAllAvailableIcons(this);
        String currentIconName = CategoryManager.getCategoryIconName(this, categoryName);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (IconInfo info : icons) {
            View itemView = inflater.inflate(R.layout.item_icon_picker, flexbox, false);
            ImageView ivIcon = itemView.findViewById(R.id.iv_icon);
            TextView tvLabel = itemView.findViewById(R.id.tv_label);

            ivIcon.setImageResource(info.resId);
            tvLabel.setText(info.label);

            // Highlight current selection
            boolean isSelected = info.resName.equals(currentIconName);
            itemView.setAlpha(isSelected ? 1.0f : 0.5f);

            itemView.setOnClickListener(v -> {
                if (info.resName.equals(currentIconName)) {
                    // Deselect - restore default
                    CategoryManager.setCategoryIcon(CategorySettingsActivity.this, categoryName, null);
                } else {
                    CategoryManager.setCategoryIcon(CategorySettingsActivity.this, categoryName, info.resName);
                }
                if (isExpense) {
                    expenseAdapter.notifyItemChanged(position);
                } else {
                    incomeAdapter.notifyItemChanged(position);
                }
                dialog.dismiss();
            });

            flexbox.addView(itemView);
        }

        dialog.show();
    }

    /**
     * RecyclerView adapter for category settings with icon support
     */
    private static class CategorySettingsAdapter extends RecyclerView.Adapter<CategorySettingsAdapter.ViewHolder> {

        private final Context context;
        private final List<String> items;
        private final OnCategoryActionListener onEditListener;
        private final OnCategoryActionListener onDeleteListener;
        private final OnCategoryActionListener onIconClickListener;

        interface OnCategoryActionListener {
            void onAction(String name, int position);
        }

        CategorySettingsAdapter(Context context, List<String> items,
                                OnCategoryActionListener editListener,
                                OnCategoryActionListener deleteListener,
                                OnCategoryActionListener iconClickListener) {
            this.context = context;
            this.items = items;
            this.onEditListener = editListener;
            this.onDeleteListener = deleteListener;
            this.onIconClickListener = iconClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String name = items.get(position);
            holder.tvName.setText(name);

            // Check custom icon mapping first
            String customIconName = CategoryManager.getCategoryIconName(context, name);
            int iconResId;
            if (customIconName != null && !customIconName.isEmpty()) {
                iconResId = context.getResources().getIdentifier(customIconName, "drawable", context.getPackageName());
            } else {
                iconResId = CategoryIconHelper.getIconResId(name);
            }
            int bgColorRes = getCategoryBgColor(name);
            int bgColor = context.getColor(bgColorRes);

            if (iconResId != 0) {
                holder.ivIcon.setImageResource(iconResId);
                holder.ivIcon.setBackground(createRoundedBg(bgColor));
                holder.ivIcon.setVisibility(View.VISIBLE);
                holder.tvIcon.setVisibility(View.GONE);
            } else {
                holder.tvIcon.setText(getCategoryEmoji(name));
                holder.tvIcon.setBackground(createRoundedBg(bgColor));
                holder.tvIcon.setVisibility(View.VISIBLE);
                holder.ivIcon.setVisibility(View.GONE);
            }

            // Click on icon to change
            View iconContainer = holder.itemView.findViewById(R.id.icon_container);
            if (iconContainer != null) {
                iconContainer.setOnClickListener(v -> {
                    if (onIconClickListener != null) onIconClickListener.onAction(name, holder.getAdapterPosition());
                });
            }

            holder.btnEdit.setOnClickListener(v -> {
                if (onEditListener != null) onEditListener.onAction(name, holder.getAdapterPosition());
            });
            holder.btnDelete.setOnClickListener(v -> {
                if (onDeleteListener != null) onDeleteListener.onAction(name, holder.getAdapterPosition());
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        private GradientDrawable createRoundedBg(int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            float radius = 12 * context.getResources().getDisplayMetrics().density;
            drawable.setCornerRadius(radius);
            drawable.setColor(color);
            return drawable;
        }

        private int getCategoryBgColor(String category) {
            if (category == null) return R.color.cat_other;
            switch (category) {
                case "\u9910\u996E": return R.color.cat_food;
                case "\u8D2D\u7269": return R.color.cat_shopping;
                case "\u4EA4\u901A": return R.color.cat_transport;
                case "\u65C5\u884C": return R.color.cat_travel;
                case "\u901A\u8BAF": return R.color.cat_communication;
                case "\u5A31\u4E50": return R.color.cat_entertainment;
                case "\u4F4F\u623F": return R.color.cat_housing;
                case "\u4EBA\u60C5": return R.color.cat_social;
                case "\u533B\u7597": return R.color.cat_medical;
                case "\u6559\u80B2": return R.color.cat_education;
                case "\u7F8E\u5BB9": return R.color.cat_beauty;
                case "\u8F6C\u8D26": return R.color.cat_transfer;
                case "\u7EA2\u5305": return R.color.cat_redpacket;
                case "\u9000\u6B3E": return R.color.cat_refund;
                default: return R.color.cat_other;
            }
        }

        private String getCategoryEmoji(String category) {
            if (category == null) return "\u2753";
            switch (category) {
                case "\u9910\u996E": return "\uD83C\uDF72";
                case "\u8D2D\u7269": return "\uD83D\uDED2";
                case "\u4EA4\u901A": return "\uD83D\uDE8C";
                case "\u65C5\u884C": return "\u2708\uFE0F";
                case "\u901A\u8BAF": return "\uD83D\uDCF1";
                case "\u5A31\u4E50": return "\uD83C\uDFAE";
                case "\u4F4F\u623F": return "\uD83C\uDFE0";
                case "\u4EBA\u60C5": return "\uD83C\uDF81";
                case "\u533B\u7597": return "\uD83D\uDC8A";
                case "\u6559\u80B2": return "\uD83D\uDCDA";
                case "\u7F8E\u5BB9": return "\uD83D\uDC84";
                case "\u8F6C\u8D26": return "\uD83D\uDCB0";
                case "\u7EA2\u5305": return "\uD83E\uDDE7";
                case "\u9000\u6B3E": return "\uD83D\uDCB5";
                case "\u5176\u4ED6": return "\u2753";
                default: return "\u2753";
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvIcon;
            ImageView ivIcon, btnEdit, btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_category_name);
                tvIcon = itemView.findViewById(R.id.tv_category_icon);
                ivIcon = itemView.findViewById(R.id.iv_category_icon);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}