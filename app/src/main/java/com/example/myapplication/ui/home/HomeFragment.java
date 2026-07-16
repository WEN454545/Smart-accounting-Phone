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

    private ImageView ivHomeBg, ivHomeHeaderBg;
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

        ivHomeBg = view.findViewById(R.id.iv_home_bg);

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



        // Load home background

        loadHomeBackground();

        // Load home header background
        loadHomeHeaderBackground();



        // 主动请求通知权限（Android 13+）

        requestNotificationPermission();



        // 点击日期弹出年月选择器

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

    private void loadHomeBackground() {
        String savedPath = profilePrefs.getString("home_bg_uri", null);
        if (savedPath != null) {
            try {
                if (savedPath.startsWith("content://")) {
                    Uri uri = Uri.parse(savedPath);
                    ivHomeBg.setImageURI(uri);
                } else {
                    ivHomeBg.setImageURI(Uri.fromFile(new java.io.File(savedPath)));
                }
                ivHomeBg.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivHomeBg.setVisibility(View.GONE);
            }
        } else {
            ivHomeBg.setVisibility(View.GONE);
        }
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
        loadHomeBackground();
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



        // 双 NumberPicker 年月选择器

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

                "1月", "2月", "3月", "4月", "5月", "6月",

                "7月", "8月", "9月", "10月", "11月", "12月"

        });



        LinearLayout layout = new LinearLayout(requireContext());

        layout.setOrientation(LinearLayout.HORIZONTAL);

        layout.setPadding(32, 16, 32, 0);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        layout.addView(yearPicker, lp);

        layout.addView(monthPicker, lp);



        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)

                .setTitle("选择年月")

                .setView(layout)

                .setPositiveButton("确定", (d, w) -> {

                    int selectedYear = yearPicker.getValue();

                    int selectedMonth = monthPicker.getValue() - 1; // 转为 Calendar.MONTH (0-11)

                    viewModel.goToYearMonth(selectedYear, selectedMonth);

                })

                .setNegativeButton("取消", null)

                .create();

        dialog.show();

    }



    private void observeData() {

        viewModel.getYear().observe(getViewLifecycleOwner(), y -> {

            Integer m = viewModel.getMonth().getValue();

            if (m != null) dateText.setText(y + "年" + (m + 1) + "月");

        });

        viewModel.getMonth().observe(getViewLifecycleOwner(), m -> {

            Integer y = viewModel.getYear().getValue();

            if (y != null) dateText.setText(y + "年" + (m + 1) + "月");

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



        budgetText.setText("剩余 \u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", remaining));

        budgetUsedText.setText("已用 \u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", used) + " / \u00A5" + String.format(java.util.Locale.CHINA, "%,.2f", total));

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

                .setTitle("删除账单")

                .setMessage("确定要删除 " + bill.getType() + " \u00A5" + String.format(java.util.Locale.CHINA, "%.2f", Math.abs(bill.getAmount())) + " 吗？")

                .setPositiveButton("删除", (d, w) -> {

                    viewModel.deleteBill(bill);

                    Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show();

                })

                .setNegativeButton("取消", null)

                .show();

    }

}