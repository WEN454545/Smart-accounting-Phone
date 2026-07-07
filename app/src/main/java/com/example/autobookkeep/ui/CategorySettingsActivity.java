package com.example.autobookkeep.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.autobookkeep.util.CategoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 记账分类预设页面 — 管理支出/收入分类
 */
public class CategorySettingsActivity extends AppCompatActivity {

    private RecyclerView rvExpense, rvIncome;
    private CategoryAdapter expenseAdapter, incomeAdapter;
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

        expenseAdapter = new CategoryAdapter(expenseList, (name, position) -> {
            showEditDialog(name, true, position);
        }, (name, position) -> {
            showDeleteDialog(name, true, position);
        });
        incomeAdapter = new CategoryAdapter(incomeList, (name, position) -> {
            showEditDialog(name, false, position);
        }, (name, position) -> {
            showDeleteDialog(name, false, position);
        });

        rvExpense.setLayoutManager(new LinearLayoutManager(this));
        rvExpense.setAdapter(expenseAdapter);
        rvIncome.setLayoutManager(new LinearLayoutManager(this));
        rvIncome.setAdapter(incomeAdapter);

        findViewById(R.id.btn_add_expense).setOnClickListener(v -> showAddDialog(true));
        findViewById(R.id.btn_add_income).setOnClickListener(v -> showAddDialog(false));
    }

    private void showAddDialog(boolean isExpense) {
        EditText input = new EditText(this);
        input.setHint("输入分类名称");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int dp16 = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(dp16, dp16, dp16, dp16);
        input.setLayoutParams(lp);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isExpense ? "添加支出分类" : "添加收入分类")
                .setView(input)
                .setPositiveButton("确定", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<String> list = isExpense ? expenseList : incomeList;
                    if (list.contains(name)) {
                        Toast.makeText(this, "分类已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    list.add(name);
                    saveAndRefresh(isExpense);
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void showEditDialog(String oldName, boolean isExpense, int position) {
        if ("自定义".equals(oldName)) {
            Toast.makeText(this, "\"自定义\"是保留分类，不可编辑", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setText(oldName);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int dp16 = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(dp16, dp16, dp16, dp16);
        input.setLayoutParams(lp);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("编辑分类名称")
                .setView(input)
                .setPositiveButton("确定", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<String> list = isExpense ? expenseList : incomeList;
                    if (!newName.equals(oldName) && list.contains(newName)) {
                        Toast.makeText(this, "分类已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    list.set(position, newName);
                    saveAndRefresh(isExpense);
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void showDeleteDialog(String name, boolean isExpense, int position) {
        if ("自定义".equals(name)) {
            Toast.makeText(this, "\"自定义\"是保留分类，不可删除", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("删除分类")
                .setMessage("确定要删除「" + name + "」吗？")
                .setPositiveButton("删除", (d, w) -> {
                    List<String> list = isExpense ? expenseList : incomeList;
                    list.remove(position);
                    saveAndRefresh(isExpense);
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
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

    // Inner adapter
    private static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

        private final List<String> items;
        private final OnCategoryClickListener onClickListener;
        private final OnCategoryLongClickListener onLongClickListener;

        interface OnCategoryClickListener {
            void onCategoryClick(String name, int position);
        }

        interface OnCategoryLongClickListener {
            void onCategoryLongClick(String name, int position);
        }

        CategoryAdapter(List<String> items, OnCategoryClickListener click,
                        OnCategoryLongClickListener longClick) {
            this.items = items;
            this.onClickListener = click;
            this.onLongClickListener = longClick;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            int dp12 = (int) (12 * parent.getContext().getResources().getDisplayMetrics().density);
            tv.setPadding(dp12, dp12, dp12, dp12);
            tv.setTextSize(15);
            tv.setTextColor(parent.getContext().getResources().getColor(R.color.ink));
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String name = items.get(position);
            holder.textView.setText(name);
            holder.textView.setOnClickListener(v -> {
                if (onClickListener != null) onClickListener.onCategoryClick(name, position);
            });
            holder.textView.setOnLongClickListener(v -> {
                if (onLongClickListener != null) onLongClickListener.onCategoryLongClick(name, position);
                return true;
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(TextView tv) { super(tv); textView = tv; }
        }
    }
}