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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy\u5E74M\u6708d\u65E5 HH:mm", Locale.CHINA);
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
        FrameLayout frameCategoryPicker = view.findViewById(R.id.frame_category_picker);
        TextView tvSelectedCategory = view.findViewById(R.id.tv_selected_category);
        EditText noteInput = view.findViewById(R.id.input_note);
        TextView dateInput = view.findViewById(R.id.input_date);
        Button btnExpense = view.findViewById(R.id.btn_expense);
        Button btnIncome = view.findViewById(R.id.btn_income);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        CategoryManager.initDefaults(requireContext());
        List<String> expenseCategories = CategoryManager.getExpenseCategories(requireContext());
        List<String> incomeCategories = CategoryManager.getIncomeCategories(requireContext());

        final String[] selectedCategory = {expenseCategories.get(0)};
        tvSelectedCategory.setText(selectedCategory[0]);

        // Category picker click - show selection dialog
        frameCategoryPicker.setOnClickListener(pv -> {
            List<String> cats = isExpense ? expenseCategories : incomeCategories;
            String[] catArray = cats.toArray(new String[0]);
            new AlertDialog.Builder(requireContext())
                    .setTitle(isExpense ? "\u652F\u51FA\u5206\u7C7B" : "\u6536\u5165\u5206\u7C7B")
                    .setItems(catArray, (d, which) -> {
                        selectedCategory[0] = catArray[which];
                        tvSelectedCategory.setText(selectedCategory[0]);
                        // Sync note if it matches the previous category
                        String currentNote = noteInput.getText().toString().trim();
                        if (editBill != null && !currentNote.isEmpty() && currentNote.equals(editBill.getType())) {
                            noteInput.setText(selectedCategory[0]);
                        }
                    })
                    .setNegativeButton("\u53D6\u6D88", null)
                    .show();
        });

        // Set default date
        dateInput.setText(dateFormat.format(selectedDate));

        // Edit mode - prefill fields
        if (editBill != null) {
            amountInput.setText(String.valueOf(Math.abs(editBill.getAmount())));
            noteInput.setText(editBill.getNote());
            selectedDate = editBill.getTimestamp();
            dateInput.setText(dateFormat.format(selectedDate));
            userId = editBill.getUserId();
            if ("income".equals(editBill.getCategory())) {
                isExpense = false;
                setIncomeStyle(btnIncome, btnExpense);
                String cat = editBill.getType();
                if (incomeCategories.contains(cat)) {
                    selectedCategory[0] = cat;
                    tvSelectedCategory.setText(cat);
                }
            } else {
                setExpenseStyle(btnExpense, btnIncome);
                String cat = editBill.getType();
                if (expenseCategories.contains(cat)) {
                    selectedCategory[0] = cat;
                    tvSelectedCategory.setText(cat);
                }
            }
        }

        // Date & time picker
        dateInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate);
            new DatePickerDialog(requireContext(),
                    (dialogView, year, month, dayOfMonth) -> {
                        // Date selected, then show time picker
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
            selectedCategory[0] = expenseCategories.get(0);
            tvSelectedCategory.setText(selectedCategory[0]);
        });

        btnIncome.setOnClickListener(v -> {
            isExpense = false;
            setIncomeStyle(btnIncome, btnExpense);
            selectedCategory[0] = incomeCategories.get(0);
            tvSelectedCategory.setText(selectedCategory[0]);
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (amountStr.isEmpty()) {
                amountInput.setError("\u8BF7\u8F93\u5165\u91D1\u989D");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (isExpense) amount = -Math.abs(amount);

            String cat = selectedCategory[0];
            String note = noteInput.getText().toString().trim();
            // In edit mode: sync note if it equals original category
            if (editBill != null && note.equals(editBill.getType())) {
                note = cat;
            }
            String incomeExpense = isExpense ? "expense" : "income";

            Bill bill;
            if (editBill != null) {
                // Edit mode: modify original object, preserve id for Room @Update
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