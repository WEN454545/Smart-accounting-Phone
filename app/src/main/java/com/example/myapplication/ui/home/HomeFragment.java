package com.example.myapplication.ui.home;

import android.app.AlertDialog;

import android.content.Context;

import android.content.Intent;

import android.content.SharedPreferences;

import android.content.pm.PackageManager;

import android.Manifest;

import android.net.Uri;

import android.os.Build;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import android.widget.LinearLayout;

import android.widget.NumberPicker;

import android.widget.ProgressBar;

import android.widget.TextView;

import android.widget.Toast;


import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;


import com.example.myapplication.R;

import com.example.myapplication.data.SessionManager;

import com.example.myapplication.data.entity.Bill;

import com.example.myapplication.data.entity.Budget;

import com.example.myapplication.data.notification.BudgetNotificationHelper;

import com.example.myapplication.ui.adapter.BillAdapter;

import com.example.myapplication.ui.adapter.DaySectionedBillAdapter;

import com.example.myapplication.ui.dialog.AddBillDialog;

import com.example.myapplication.util.ImageUtils;


import java.util.Calendar;


public class HomeFragment extends Fragment implements BillAdapter.OnBillClickListener, BillAdapter.OnBillLongClickListener {


    private TextView incomeText, expenseText, budgetText, budgetUsedText, budgetPercentText, dateText;

    private ProgressBar budgetProgress;

    private RecyclerView recyclerView;

    private DaySectionedBillAdapter adapter;

    private HomeViewModel viewModel;

    private double currentUsedExpense = 0;

    private BudgetNotificationHelper notificationHelper;

    private SharedPreferences profilePrefs;

    private ImageView ivHomeHeaderBg;
    private View vHeaderMask;


    @Nullable

    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        SessionManager sessionManager = new SessionManager(requireContext());

        long userId = sessionManager.getUserId();

        notificationHelper = new BudgetNotificationHelper(requireContext(), userId);

        profilePrefs = requireContext().getSharedPreferences("profile_settings", Context.MODE_PRIVATE);


        incomeText = view.findViewById(R.id.tv_income);

        expenseText = view.findViewById(R.id.tv_expense);

        budgetText = view.findViewById(R.id.tv_budget_remaining);

        budgetUsedText = view.findViewById(R.id.tv_budget_used);

        budgetPercentText = view.findViewById(R.id.tv_budget_percent);

        budgetProgress = view.findViewById(R.id.budget_progress);

        recyclerView = view.findViewById(R.id.recycler_bills);

        dateText = view.findViewById(R.id.tv_date);

        ivHomeHeaderBg = view.findViewById(R.id.iv_home_header_bg);
        vHeaderMask = view.findViewById(R.id.v_header_mask);

        // Constrain header background image to the header content area
        LinearLayout headerContent = view.findViewById(R.id.ll_header_content);
        headerContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = headerContent.getHeight();
                if (height > 0) {
                    ViewGroup.LayoutParams imgParams = ivHomeHeaderBg.getLayoutParams();
                    imgParams.height = height;
                    ivHomeHeaderBg.setLayoutParams(imgParams);
                    ViewGroup.LayoutParams maskParams = vHeaderMask.getLayoutParams();
                    maskParams.height = height;
                    vHeaderMask.setLayoutParams(maskParams);
                }
                headerContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        // Load home header background
        loadHomeHeaderBackground();


        // \u4E3B\u52A8\u8BF7\u6C42\u901A\u77E5\u6743\u9650\uFF08Android 13+\uFF09

        requestNotificationPermission();


        // \u70B9\u51FB\u65E5\u671F\u5F39\u51FA\u5E74\u6708\u9009\u62E9\u5668

        dateText.setOnClickListener(v -> showYearMonthPicker());


        adapter = new DaySectionedBillAdapter(this);

        adapter.setOnLongClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerView.setAdapter(adapter);


        view.findViewById(R.id.fab_add).setOnClickListener(v -> showAddDialog(null));


        view.findViewById(R.id.tv_view_all).setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager()

                    .beginTransaction()

                    .replace(R.id.fragment_container, new AllBillsFragment())

                    .addToBackStack(null)

                    .commit();

        });


        view.findViewById(R.id.iv_settings).setOnClickListener(v -> {

            Intent intent = new Intent(requireActivity(), com.example.autobookkeep.ui.AutoSettingsActivity.class);

            startActivity(intent);

        });


        observeData();
        return view;

    }

    private void loadHomeHeaderBackground() {
        String savedPath = profilePrefs.getString("home_header_bg_uri", null);
        if (savedPath != null) {
            try {
                if (savedPath.startsWith("content://")) {
                    Uri uri = Uri.parse(savedPath);
                    String cacheKey = ImageUtils.getCacheKey(uri);
                    ImageUtils.loadBackgroundImage(requireContext(), ivHomeHeaderBg, uri, cacheKey, ImageView.ScaleType.CENTER_CROP);
                } else {
                    // File path from crop
                    ivHomeHeaderBg.setImageURI(Uri.fromFile(new java.io.File(savedPath)));
                }
                ivHomeHeaderBg.setVisibility(View.VISIBLE);
                vHeaderMask.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivHomeHeaderBg.setVisibility(View.GONE);
                vHeaderMask.setVisibility(View.GONE);
            }
        } else {
            ivHomeHeaderBg.setVisibility(View.GONE);
            vHeaderMask.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHomeHeaderBackground();
    }

    private static final int REQUEST_CODE_NOTIFICATION = 2001;


    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)

                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),

                        new String[]{Manifest.permission.POST_NOTIFICATIONS},

                        REQUEST_CODE_NOTIFICATION);

            }

        }

    }


    private void showYearMonthPicker() {

        Integer curYear = viewModel.getYear().getValue();

        Integer curMonth = viewModel.getMonth().getValue();

        if (curYear == null || curMonth == null) {

            Calendar cal = Calendar.getInstance();

            curYear = cal.get(Calendar.YEAR);

            curMonth = cal.get(Calendar.MONTH);

        }


        int yearVal = curYear;

        int monthVal = curMonth;


        // \u53CC NumberPicker \u5E74\u6708\u9009\u62E9\u5668

        NumberPicker yearPicker = new NumberPicker(requireContext());

        yearPicker.setMinValue(2015);

        yearPicker.setMaxValue(2030);

        yearPicker.setValue(yearVal);

        yearPicker.setWrapSelectorWheel(false);


        NumberPicker monthPicker = new NumberPicker(requireContext());

        monthPicker.setMinValue(1);

        monthPicker.setMaxValue(12);

        monthPicker.setValue(monthVal + 1);

        monthPicker.setWrapSelectorWheel(true);

        monthPicker.setDisplayedValues(new String[]{

                "1\u6708", "2\u6708", "3\u6708", "4\u6708", "5\u6708", "6\u6708",

                "7\u6708", "8\u6708", "9\u6708", "10\u6708", "11\u6708", "12\u6708"

        });


        LinearLayout layout = new LinearLayout(requireContext());

        layout.setOrientation(LinearLayout.HORIZONTAL);

        layout.setPadding(32, 16, 32, 0);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        layout.addView(yearPicker, lp);

        layout.addView(monthPicker, lp);


        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)

                .setTitle("\u9009\u62E9\u5E74\u6708")

                .setView(layout)

                .setPositiveButton("\u786E\u5B9A", (d, w) -> {

                    int selectedYear = yearPicker.getValue();

                    int selectedMonth = monthPicker.getValue() - 1; // \u8F6C\u4E3A Calendar.MONTH (0-11)

                    viewModel.goToYearMonth(selectedYear, selectedMonth);

                })

                .setNegativeButton("\u53D6\u6D88", null)

                .create();

        dialog.show();

    }


    private void observeData() {

        viewModel.getYear().observe(getViewLifecycleOwner(), y -> {

            Integer m = viewModel.getMonth().getValue();

            if (m != null) dateText.setText(y + "\u5E74" + (m + 1) + "\u6708");

        });

        viewModel.getMonth().observe(getViewLifecycleOwner(), m -> {

            Integer y = viewModel.getYear().getValue();

            if (y != null) dateText.setText(y + "\u5E74" + (m + 1) + "\u6708");

        });


        viewModel.getIncome().observe(getViewLifecycleOwner(), income -> {

            double val = income != null ? income : 0;

            incomeText.setText("\u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", val));

        });


        viewModel.getExpense().observe(getViewLifecycleOwner(), expense -> {

            double val = expense != null ? expense : 0;

            currentUsedExpense = val;

            expenseText.setText("\u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", val));

            updateBudgetDisplay(val);

        });


        viewModel.getBills().observe(getViewLifecycleOwner(), bills ->

                adapter.setBills(bills != null ? bills : java.util.Collections.emptyList()));


        viewModel.getBudget().observe(getViewLifecycleOwner(), budget ->

                updateBudgetDisplay(currentUsedExpense));

    }


    private void updateBudgetDisplay(double used) {

        Budget budget = viewModel.getBudget().getValue();

        double total = (budget != null && budget.getTotalBudget() > 0) ? budget.getTotalBudget() : 5000;

        double remaining = Math.max(0, total - used);

        int percent = (int) Math.min(100, Math.max(0, (used / total) * 100));


        budgetText.setText("\u5269\u4F59 \u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", remaining));

        budgetUsedText.setText("\u5DF2\u7528 \u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", used) + " / \u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", total));

        budgetPercentText.setText(percent + "%");

        budgetProgress.setProgress(percent);


        notificationHelper.checkAndNotify(used, total);

    }


    private void showAddDialog(@Nullable Bill editBill) {

        AddBillDialog dialog = new AddBillDialog();

        if (editBill != null) dialog.setEditBill(editBill);

        dialog.setOnSaveListener(bill -> {

            if (editBill != null) {

                viewModel.updateBill(bill);

            } else {

                viewModel.insertBill(bill);

            }

        });

        dialog.show(getParentFragmentManager(), "add");

    }


    @Override

    public void onBillClick(Bill bill) {

        showAddDialog(bill);

    }


    @Override

    public void onBillLongClick(Bill bill) {

        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)

                .setTitle("\u5220\u9664\u8D26\u5355")

                .setMessage("\u786E\u5B9A\u8981\u5220\u9664 " + bill.getType() + " \u00A5" + String.format(java.util.Locale.CHINA, "%.2f", Math.abs(bill.getAmount())) + " \u5417\uFF1F")

                .setPositiveButton("\u5220\u9664", (d, w) -> {

                    viewModel.deleteBill(bill);

                    Toast.makeText(requireContext(), "\u5DF2\u5220\u9664", Toast.LENGTH_SHORT).show();

                })

                .setNegativeButton("\u53D6\u6D88", null)

                .show();

    }

}