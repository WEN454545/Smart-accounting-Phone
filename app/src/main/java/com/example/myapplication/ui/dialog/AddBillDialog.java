package com.example.myapplication.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Bill;
import com.example.autobookkeep.util.CategoryManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddBillDialog extends DialogFragment {

    private static final String PREF_NAME = "profile_settings";
    private static final String KEY_DIALOG_BILL_BG_URI = "dialog_bill_bg_uri";

    public interface OnSaveListener {
        void onSave(Bill bill);
    }

    private OnSaveListener listener;
    private Bill editBill;
    private boolean isExpense = true;
    private long selectedDate = System.currentTimeMillis();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA);
    private long userId;

    public void setOnSaveListener(OnSaveListener listener) {
        this.listener = listener;
    }

    /** Set an existing bill to edit; null = create new */
    public void setEditBill(Bill bill) {
        this.editBill = bill;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getUserId();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bill, null);

        EditText amountInput = view.findViewById(R.id.input_amount);
        Spinner categorySpinner = view.findViewById(R.id.input_category);
        EditText noteInput = view.findViewById(R.id.input_note);
        TextView dateInput = view.findViewById(R.id.input_date);
        Button btnExpense = view.findViewById(R.id.btn_expense);
        Button btnIncome = view.findViewById(R.id.btn_income);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        CategoryManager.initDefaults(requireContext());
        List<String> expenseCategories = CategoryManager.getExpenseCategories(requireContext());
        List<String> incomeCategories = CategoryManager.getIncomeCategories(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new ArrayList<>(expenseCategories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // 用户切换分类时，若备注是自动填充的（等于原始分类名或上一个选中分类），同步更新
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private String previousCategory = "";

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newCategory = parent.getItemAtPosition(position).toString();
                String currentNote = noteInput.getText().toString().trim();
                // 编辑模式下：若备注等于原始分类名或上一个选中分类，同步更新（空备注不自动填充）
                if (editBill != null) {
                    if (!currentNote.isEmpty() && (currentNote.equals(editBill.getType()) || currentNote.equals(previousCategory))) {
                        noteInput.setText(newCategory);
                    }
                }
                previousCategory = newCategory;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set default date
        dateInput.setText(dateFormat.format(selectedDate));

        // Edit mode — prefill fields
        if (editBill != null) {
            amountInput.setText(String.valueOf(Math.abs(editBill.getAmount())));
            noteInput.setText(editBill.getNote());
            selectedDate = editBill.getTimestamp();
            dateInput.setText(dateFormat.format(selectedDate));
            userId = editBill.getUserId();
            if ("income".equals(editBill.getCategory())) {
                isExpense = false;
                setIncomeStyle(btnIncome, btnExpense);
                adapter.clear();
                adapter.addAll(incomeCategories);
                adapter.notifyDataSetChanged();
                String cat = editBill.getType();
                int idx = incomeCategories.indexOf(cat);
                if (idx >= 0) categorySpinner.setSelection(idx);
            } else {
                setExpenseStyle(btnExpense, btnIncome);
                String cat = editBill.getType();
                int idx = expenseCategories.indexOf(cat);
                if (idx >= 0) categorySpinner.setSelection(idx);
            }
        }

        // Date & time picker
        dateInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate);
            new DatePickerDialog(requireContext(),
                    (dialogView, year, month, dayOfMonth) -> {
                        // 日期选完后弹出时间选择器
                        Calendar calTmp = Calendar.getInstance();
                        calTmp.setTimeInMillis(selectedDate);
                        new TimePickerDialog(requireContext(),
                                (timeView, hourOfDay, minute) -> {
                                    Calendar selected = Calendar.getInstance();
                                    selected.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                    selected.set(Calendar.MILLISECOND, 0);
                                    selectedDate = selected.getTimeInMillis();
                                    dateInput.setText(dateFormat.format(selectedDate));
                                },
                                calTmp.get(Calendar.HOUR_OF_DAY),
                                calTmp.get(Calendar.MINUTE),
                                true
                        ).show();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnExpense.setOnClickListener(v -> {
            isExpense = true;
            setExpenseStyle(btnExpense, btnIncome);
            adapter.clear();
            adapter.addAll(expenseCategories);
            adapter.notifyDataSetChanged();
            categorySpinner.setSelection(0);
        });

        btnIncome.setOnClickListener(v -> {
            isExpense = false;
            setIncomeStyle(btnIncome, btnExpense);
            adapter.clear();
            adapter.addAll(incomeCategories);
            adapter.notifyDataSetChanged();
            categorySpinner.setSelection(0);
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (amountStr.isEmpty()) {
                amountInput.setError("请输入金额");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (isExpense) amount = -Math.abs(amount);

            String cat = categorySpinner.getSelectedItem().toString();
            String note = noteInput.getText().toString().trim();
            // 编辑模式下：若备注等于原分类，同步更新为新分类
            if (editBill != null && note.equals(editBill.getType())) {
                note = cat;
            }
            String incomeExpense = isExpense ? "expense" : "income";

            Bill bill;
            if (editBill != null) {
                // 编辑模式：直接修改原始对象，保留 id，确保 Room @Update 正确匹配
                editBill.setType(cat);
                editBill.setAmount(amount);
                editBill.setTimestamp(selectedDate);
                editBill.setNote(note);
                editBill.setCategory(incomeExpense);
                editBill.setSource("manual");
                bill = editBill;
            } else {
                bill = new Bill(cat, amount, selectedDate, "", note, "manual",
                        incomeExpense, userId);
            }

            if (listener != null) listener.onSave(bill);
            dismiss();
        });

        builder.setView(view);

        AlertDialog d = builder.create();
        d.setOnShowListener(dialogInterface -> {
            // Make internal panels transparent so the custom window background shows through
            View parentPanel = d.findViewById(androidx.appcompat.R.id.parentPanel);
            if (parentPanel != null) {
                parentPanel.setBackground(null);
            }
            View contentPanel = d.findViewById(androidx.appcompat.R.id.contentPanel);
            if (contentPanel != null) {
                contentPanel.setBackground(null);
            }
        });
        if (d.getWindow() != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String bgUriStr = prefs.getString(KEY_DIALOG_BILL_BG_URI, null);
            if (bgUriStr != null) {
                try {
                    Bitmap bitmap = decodeSampledBitmap(bgUriStr, 600);
                    if (bitmap != null) {
                        float density = getResources().getDisplayMetrics().density;
                        RoundedBitmapDrawable roundedBg = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBg.setCornerRadius(32 * density);
                        roundedBg.setAntiAlias(true);
                        d.getWindow().setBackgroundDrawable(roundedBg);
                    } else {
                        d.getWindow().setBackgroundDrawableResource(R.drawable.bg_bottom_sheet_rounded);
                    }
                } catch (Exception e) {
                    d.getWindow().setBackgroundDrawableResource(R.drawable.bg_bottom_sheet_rounded);
                }
            } else {
                d.getWindow().setBackgroundDrawableResource(R.drawable.bg_bottom_sheet_rounded);
            }
        }
        return d;
    }

    private void setExpenseStyle(Button btnExpense, Button btnIncome) {
        btnExpense.setBackgroundColor(requireContext().getColor(R.color.expense_bg));
        btnExpense.setTextColor(requireContext().getColor(R.color.expense));
        btnIncome.setBackgroundColor(requireContext().getColor(R.color.bg));
        btnIncome.setTextColor(requireContext().getColor(R.color.muted));
    }

    private void setIncomeStyle(Button btnIncome, Button btnExpense) {
        btnIncome.setBackgroundColor(requireContext().getColor(R.color.income_bg));
        btnIncome.setTextColor(requireContext().getColor(R.color.income));
        btnExpense.setBackgroundColor(requireContext().getColor(R.color.bg));
        btnExpense.setTextColor(requireContext().getColor(R.color.muted));
    }

    /**
     * Decode a bitmap from the given URI/file path, scaled to fit within maxWidth pixels.
     * Uses inSampleSize for memory-efficient decoding, then scales to exact target width.
     */
    private Bitmap decodeSampledBitmap(String path, int maxWidth) {
        try {
            // First decode bounds to get original dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            if (path.startsWith("content://")) {
                BitmapFactory.decodeStream(
                        requireContext().getContentResolver().openInputStream(Uri.parse(path)),
                        null, options);
            } else {
                BitmapFactory.decodeFile(path, options);
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxWidth, maxWidth);
            options.inJustDecodeBounds = false;

            Bitmap bitmap;
            if (path.startsWith("content://")) {
                bitmap = BitmapFactory.decodeStream(
                        requireContext().getContentResolver().openInputStream(Uri.parse(path)),
                        null, options);
            } else {
                bitmap = BitmapFactory.decodeFile(path, options);
            }

            if (bitmap == null) return null;

            // Scale to exact target width while maintaining aspect ratio
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxWidth) {
                float ratio = (float) maxWidth / width;
                int newHeight = Math.round(height * ratio);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                }
                return scaled;
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            int halfHeight = rawHeight / 2;
            int halfWidth = rawWidth / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    }
