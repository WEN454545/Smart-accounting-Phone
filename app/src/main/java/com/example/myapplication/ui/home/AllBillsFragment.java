package com.example.myapplication.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.ui.adapter.BillAdapter;
import com.example.myapplication.ui.adapter.SectionedBillAdapter;
import com.example.myapplication.ui.dialog.AddBillDialog;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AllBillsFragment extends Fragment implements BillAdapter.OnBillClickListener, BillAdapter.OnBillLongClickListener {

    private RecyclerView recyclerView;
    private SectionedBillAdapter adapter;
    private AllBillsViewModel viewModel;
    private ImageView btnClearSearch;
    private ImageView btnFilter;
    private View emptyView;
    /** Debounces search input so we do not rescan + regroup on every keystroke. */
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_bills, container, false);

        recyclerView = view.findViewById(R.id.recycler_all_bills);
        adapter = new SectionedBillAdapter(this);
        adapter.setOnLongClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        emptyView = view.findViewById(R.id.tv_empty_bills);
        btnFilter = view.findViewById(R.id.btn_filter);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btn_delete_all).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                    .setTitle(R.string.delete_all_confirm_title)
                    .setMessage(R.string.delete_all_confirm_msg)
                    .setPositiveButton("\u5220\u9664\u5168\u90E8", (d, w) -> {
                        viewModel.deleteAllBills();
                        Toast.makeText(requireContext(), R.string.delete_all_done, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("\u53D6\u6D88", null)
                    .show();
        });

        viewModel = new ViewModelProvider(this).get(AllBillsViewModel.class);

        viewModel.getFilteredBills().observe(getViewLifecycleOwner(), bills -> {
            adapter.setBills(bills != null ? bills : java.util.Collections.emptyList());
            emptyView.setVisibility(bills == null || bills.isEmpty() ? View.VISIBLE : View.GONE);
        });

        setupSearch(view.findViewById(R.id.et_search_bills));
        btnFilter.setOnClickListener(v -> showFilterDialog());

        return view;
    }

    private void setupSearch(EditText etSearch) {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (viewModel == null) return;
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> viewModel.setQuery(s.toString());
                searchHandler.postDelayed(searchRunnable, 250);
            }
        });
        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
        // Restore the active query after rotation/recreation so the input box
        // matches the already-applied ViewModel state
        String activeQuery = viewModel.getQueryValue();
        if (!activeQuery.isEmpty()) {
            etSearch.setText(activeQuery);
            etSearch.setSelection(activeQuery.length());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rebind bill icons: icon changes in settings only touch SharedPreferences,
        // which does not retrigger the bill LiveData.
        if (adapter != null) adapter.notifyDataSetChanged();
        updateFilterButtonTint();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }

    private void updateFilterButtonTint() {
        if (btnFilter == null) return;
        int color = viewModel != null && viewModel.getFilter().isActive()
                ? ContextCompat.getColor(requireContext(), R.color.primary)
                : ContextCompat.getColor(requireContext(), R.color.muted);
        btnFilter.setColorFilter(color);
    }

    /**
     * Bottom Sheet filter panel with grouped sections:
     * bill type (single choice), categories (multi-select grid),
     * time range (presets + custom date range), amount range.
     */
    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_bills, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.ThemeOverlay_RoundedBottomSheet);
        dialog.setContentView(dialogView);

        final BillFilter[] working = {viewModel.getFilter().copy()};
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        // ---- Bill type chips (single choice) ----
        TextView chipTypeAll = dialogView.findViewById(R.id.chip_type_all);
        TextView chipTypeExpense = dialogView.findViewById(R.id.chip_type_expense);
        TextView chipTypeIncome = dialogView.findViewById(R.id.chip_type_income);
        TextView[] typeChips = {chipTypeAll, chipTypeExpense, chipTypeIncome};
        int[] typeValues = {BillFilter.TYPE_ALL, BillFilter.TYPE_EXPENSE, BillFilter.TYPE_INCOME};
        View.OnClickListener typeClick = v -> {
            for (int i = 0; i < typeChips.length; i++) {
                boolean selected = v == typeChips[i];
                typeChips[i].setSelected(selected);
                typeChips[i].setTextColor(selected ? ContextCompat.getColor(requireContext(), R.color.text_white) : ContextCompat.getColor(requireContext(), R.color.ink));
                if (selected) working[0].billType = typeValues[i];
            }
        };
        for (TextView chip : typeChips) chip.setOnClickListener(typeClick);

        // ---- Category chips (multi-select grid) ----
        FlexboxLayout flexCategories = dialogView.findViewById(R.id.flex_filter_categories);
        Set<String> categoryNames = new LinkedHashSet<>(
                com.example.autobookkeep.util.CategoryManager.getExpenseCategories(requireContext()));
        categoryNames.addAll(com.example.autobookkeep.util.CategoryManager.getIncomeCategories(requireContext()));
        // Drop categories that no longer exist (renamed/deleted) so the applied
        // filter cannot keep a ghost selection that shows an empty list
        working[0].categories.retainAll(categoryNames);
        Map<String, TextView> categoryChips = new java.util.LinkedHashMap<>();
        for (String cat : categoryNames) {
            TextView chip = new TextView(requireContext());
            chip.setText(cat);
            int h = (int) (getResources().getDisplayMetrics().density * 14);
            chip.setPadding(h, (int) (6 * getResources().getDisplayMetrics().density), h, (int) (6 * getResources().getDisplayMetrics().density));
            chip.setBackgroundResource(R.drawable.bg_filter_chip);
            chip.setTextSize(13);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
            chip.setClickable(true);
            chip.setFocusable(true);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) (6 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density), 0);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                boolean nowSelected = !chip.isSelected();
                chip.setSelected(nowSelected);
                chip.setTextColor(nowSelected ? 0xFFFFFFFF : ContextCompat.getColor(requireContext(), R.color.ink));
                if (nowSelected) working[0].categories.add(cat);
                else working[0].categories.remove(cat);
            });
            flexCategories.addView(chip);
            categoryChips.put(cat, chip);
        }

        // ---- Time range chips (single choice) ----
        TextView chipTimeAll = dialogView.findViewById(R.id.chip_time_all);
        TextView chipTimeMonth = dialogView.findViewById(R.id.chip_time_month);
        TextView chipTimeYear = dialogView.findViewById(R.id.chip_time_year);
        TextView chipTimeCustom = dialogView.findViewById(R.id.chip_time_custom);
        TextView[] timeChips = {chipTimeAll, chipTimeMonth, chipTimeYear, chipTimeCustom};
        int[] rangeValues = {BillFilter.RANGE_ALL, BillFilter.RANGE_MONTH, BillFilter.RANGE_YEAR, BillFilter.RANGE_CUSTOM};
        View layoutCustomDate = dialogView.findViewById(R.id.layout_custom_date);
        TextView tvDateStart = dialogView.findViewById(R.id.tv_date_start);
        TextView tvDateEnd = dialogView.findViewById(R.id.tv_date_end);
        Runnable refreshDateTexts = () -> {
            tvDateStart.setText(working[0].customStart > 0 ? dateFmt.format(new java.util.Date(working[0].customStart)) : "");
            tvDateEnd.setText(working[0].customEnd > 0 ? dateFmt.format(new java.util.Date(working[0].customEnd)) : "");
        };
        View.OnClickListener timeClick = v -> {
            for (int i = 0; i < timeChips.length; i++) {
                boolean selected = v == timeChips[i];
                timeChips[i].setSelected(selected);
                timeChips[i].setTextColor(selected ? ContextCompat.getColor(requireContext(), R.color.text_white) : ContextCompat.getColor(requireContext(), R.color.ink));
                if (selected) working[0].range = rangeValues[i];
            }
            layoutCustomDate.setVisibility(v == chipTimeCustom ? View.VISIBLE : View.GONE);
        };
        for (TextView chip : timeChips) chip.setOnClickListener(timeClick);

        DatePickerDialog.OnDateSetListener startListener = (dp, y, m, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(y, m, day, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            working[0].customStart = c.getTimeInMillis();
            refreshDateTexts.run();
        };
        DatePickerDialog.OnDateSetListener endListener = (dp, y, m, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(y, m, day, 23, 59, 59);
            c.set(Calendar.MILLISECOND, 999);
            working[0].customEnd = c.getTimeInMillis();
            refreshDateTexts.run();
        };
        tvDateStart.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), startListener,
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        tvDateEnd.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), endListener,
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ---- Amount range ----
        EditText etMin = dialogView.findViewById(R.id.et_amount_min);
        EditText etMax = dialogView.findViewById(R.id.et_amount_max);
        if (working[0].minAmount != null) etMin.setText(String.format(Locale.CHINA, "%.2f", working[0].minAmount));
        if (working[0].maxAmount != null) etMax.setText(String.format(Locale.CHINA, "%.2f", working[0].maxAmount));

        // ---- Restore current filter state on chips ----
        Runnable restore = () -> {
            for (int i = 0; i < typeChips.length; i++) {
                boolean selected = working[0].billType == typeValues[i];
                typeChips[i].setSelected(selected);
                typeChips[i].setTextColor(selected ? ContextCompat.getColor(requireContext(), R.color.text_white) : ContextCompat.getColor(requireContext(), R.color.ink));
            }
            for (int i = 0; i < timeChips.length; i++) {
                boolean selected = working[0].range == rangeValues[i];
                timeChips[i].setSelected(selected);
                timeChips[i].setTextColor(selected ? ContextCompat.getColor(requireContext(), R.color.text_white) : ContextCompat.getColor(requireContext(), R.color.ink));
            }
            layoutCustomDate.setVisibility(working[0].range == BillFilter.RANGE_CUSTOM ? View.VISIBLE : View.GONE);
            for (Map.Entry<String, TextView> e : categoryChips.entrySet()) {
                boolean selected = working[0].categories.contains(e.getKey());
                e.getValue().setSelected(selected);
                e.getValue().setTextColor(selected ? ContextCompat.getColor(requireContext(), R.color.text_white) : ContextCompat.getColor(requireContext(), R.color.ink));
            }
            refreshDateTexts.run();
        };
        restore.run();

        // ---- Reset ----
        dialogView.findViewById(R.id.btn_filter_reset).setOnClickListener(v -> {
            working[0] = new BillFilter();
            etMin.setText("");
            etMax.setText("");
            restore.run();
        });

        // ---- Confirm ----
        dialogView.findViewById(R.id.btn_filter_confirm).setOnClickListener(v -> {
            Double min = parseAmount(etMin.getText().toString());
            Double max = parseAmount(etMax.getText().toString());
            if (min != null && max != null && min > max) {
                Toast.makeText(requireContext(), R.string.filter_invalid_amount, Toast.LENGTH_SHORT).show();
                return;
            }
            working[0].minAmount = min;
            working[0].maxAmount = max;
            if (working[0].range == BillFilter.RANGE_CUSTOM
                    && working[0].customStart <= 0 && working[0].customEnd <= 0) {
                working[0].range = BillFilter.RANGE_ALL; // custom picked but no dates chosen
            }
            viewModel.setFilter(working[0]);
            updateFilterButtonTint();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_filter_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static Double parseAmount(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void onBillClick(Bill bill) {
        AddBillDialog dialog = new AddBillDialog();
        dialog.setEditBill(bill);
        dialog.setOnSaveListener(updatedBill -> {
            updatedBill.setId(bill.getId());
            viewModel.updateBill(updatedBill);
            Toast.makeText(requireContext(), R.string.bill_updated, Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "edit");
    }

    @Override
    public void onBillLongClick(Bill bill) {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setTitle(R.string.delete_bill_title)
                .setMessage(String.format(java.util.Locale.CHINA,
                        getString(R.string.delete_bill_confirm_fmt), bill.getType(), Math.abs(bill.getAmount())))
                .setPositiveButton("\u5220\u9664", (d, w) -> {
                    viewModel.deleteBill(bill);
                    Toast.makeText(requireContext(), R.string.bill_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("\u53D6\u6D88", null)
                .show();
    }
}
