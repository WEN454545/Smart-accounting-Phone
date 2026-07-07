package com.example.autobookkeep.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.autobookkeep.PermissionUtils;
import com.example.autobookkeep.util.AssistantConfig;
import com.example.autobookkeep.util.KeywordManager;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AssistantManagerActivity extends AppCompatActivity {

    private AssistantConfig config;
    private SwitchCompat switchAutoTrack;
    private SwitchCompat switchAssets;
    private SwitchCompat switchDetails;
    private RecyclerView rvKeywords;
    private TextView tvOverlayStatus;
    private SwitchCompat switchOverlay;
    private KeywordAdapter adapter;
    private List<KeywordItem> dataList = new ArrayList<>();

    private static class KeywordItem implements Comparable<KeywordItem> {
        String packageName;
        String appName;
        String text;
        int type;

        KeywordItem(String pkg, String appName, String text, int type) {
            this.packageName = pkg;
            this.appName = appName;
            this.text = text;
            this.type = type;
        }

        @Override
        public int compareTo(KeywordItem o) {
            int appCompare = this.appName.compareTo(o.appName);
            if (appCompare != 0) return appCompare;
            if (this.type != o.type) return Integer.compare(this.type, o.type);
            return this.text.compareTo(o.text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_manager);

        config = new AssistantConfig(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        switchAutoTrack = findViewById(R.id.switchAutoTrack);
        switchAssets = findViewById(R.id.switchAssets);
        switchDetails = findViewById(R.id.switchDetails);
        rvKeywords = findViewById(R.id.rvKeywords);
        tvOverlayStatus = findViewById(R.id.tv_overlay_status);
        switchOverlay = findViewById(R.id.switchOverlay);

        switchAutoTrack.setChecked(config.isEnabled());
        switchAssets.setChecked(config.isAssetsEnabled());
        switchDetails.setChecked(config.isDetailsEnabled());
        switchOverlay.setChecked(PermissionUtils.isOverlayPermissionEnabled(this));

        switchAutoTrack.setOnCheckedChangeListener((buttonView, isChecked) -> {
            config.setEnabled(isChecked);
            if (isChecked && !PermissionUtils.isAccessibilityServiceEnabled(this)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.accessibility_title)
                        .setMessage(R.string.accessibility_message)
                        .setPositiveButton(R.string.accessibility_btn, (d, w) -> PermissionUtils.openAccessibilitySettings(this))
                        .setCancelable(false)
                        .show();
            }
            Toast.makeText(this, isChecked ? R.string.track_enabled : R.string.track_disabled, Toast.LENGTH_SHORT).show();
        });

        switchAssets.setOnCheckedChangeListener((buttonView, isChecked) -> {
            config.setAssetsEnabled(isChecked);
            Toast.makeText(this, isChecked ? R.string.assets_enabled : R.string.assets_disabled, Toast.LENGTH_SHORT).show();
        });

        switchDetails.setOnCheckedChangeListener((buttonView, isChecked) -> {
            config.setDetailsEnabled(isChecked);
            Toast.makeText(this, isChecked ? R.string.details_enabled : R.string.details_disabled, Toast.LENGTH_SHORT).show();
        });

        switchOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionUtils.isOverlayPermissionEnabled(this)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.overlay_title)
                        .setMessage(R.string.overlay_desc)
                        .setPositiveButton(R.string.accessibility_btn, (d, w) -> PermissionUtils.openOverlaySettings(this))
                        .setNegativeButton(R.string.cancel_btn, (d, w) -> switchOverlay.setChecked(false))
                        .setCancelable(false)
                        .show();
            }
        });

        rvKeywords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KeywordAdapter();
        rvKeywords.setAdapter(adapter);

        findViewById(R.id.btnAddKeyword).setOnClickListener(v -> showAddKeywordDialog());

        // 电池优化设置
        TextView tvBatteryStatus = findViewById(R.id.tv_battery_status);
        Button btnBattery = findViewById(R.id.btn_battery);
        btnBattery.setOnClickListener(v -> {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && pm.isIgnoringBatteryOptimizations(getPackageName())) {
                // 已开启电池优化，无需操作
                Toast.makeText(this, R.string.battery_already_optimized, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                try {
                    // 部分设备不支持，跳转到电池优化列表
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                } catch (Exception e2) {
                    // 最后尝试打开应用设置页
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (Exception ignored) {}
                }
            }
        });

        // 自启动管理引导
        findViewById(R.id.layout_auto_start).setOnClickListener(v -> showAutoStartGuide());
        Button btnAutoStart = findViewById(R.id.btn_auto_start);
        btnAutoStart.setOnClickListener(v -> showAutoStartGuide());
    }

    private void updateBatteryStatus() {
        TextView tvBatteryStatus = findViewById(R.id.tv_battery_status);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null && pm.isIgnoringBatteryOptimizations(getPackageName())) {
            tvBatteryStatus.setText(R.string.battery_optimized);
            tvBatteryStatus.setTextColor(getResources().getColor(R.color.income));
        } else {
            tvBatteryStatus.setText(R.string.battery_not_optimized);
            tvBatteryStatus.setTextColor(getResources().getColor(R.color.expense));
        }
    }

    private void showAutoStartGuide() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        String guide;
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
            guide = "小米/红米自启动设置：\n1. 打开「手机管家」\n2. 点击「应用管理」→「权限」\n3. 找到「智能记账」\n4. 开启「自启动」权限";
        } else if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
            guide = "华为/荣耀自启动设置：\n1. 打开「手机管家」\n2. 点击「应用启动管理」\n3. 找到「智能记账」\n4. 关闭「自动管理」，允许自启动";
        } else if (manufacturer.contains("oppo")) {
            guide = "OPPO自启动设置：\n1. 打开「手机管家」\n2. 点击「权限隐私」→「自启动管理」\n3. 找到「智能记账」并开启";
        } else if (manufacturer.contains("vivo")) {
            guide = "vivo自启动设置：\n1. 打开「i管家」\n2. 点击「应用管理」→「权限管理」\n3. 找到「智能记账」\n4. 开启「自启动」";
        } else if (manufacturer.contains("samsung")) {
            guide = "三星自启动设置：\n1. 打开「智能管理器」\n2. 点击「电池」→「应用程序管理」\n3. 找到「智能记账」\n4. 关闭「使应用程序进入休眠」";
        } else {
            guide = "请在手机设置中搜索「自启动」或「启动管理」\n找到「智能记账」并开启自启动权限\n\n如找不到，请在「电池」或「应用管理」中查找";
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.auto_start_guide_title)
                .setMessage(guide)
                .setPositiveButton(R.string.accessibility_btn, (d, w) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton(R.string.cancel_btn, null)
                .show();
    }

    private void updateOverlayStatus() {
        if (PermissionUtils.isOverlayPermissionEnabled(this)) {
            tvOverlayStatus.setText(R.string.overlay_enabled);
            tvOverlayStatus.setTextColor(getResources().getColor(R.color.income));
            switchOverlay.setChecked(true);
        } else {
            tvOverlayStatus.setText(R.string.overlay_disabled);
            tvOverlayStatus.setTextColor(getResources().getColor(R.color.text_hint));
            switchOverlay.setChecked(false);
        }
    }

    private void loadData() {
        updateOverlayStatus();
        updateBatteryStatus();
        dataList.clear();
        String[] packages = {"com.tencent.mm", "com.eg.android.AlipayGphone",
                "com.xunmeng.pinduoduo", "com.jingdong.app.mall", "com.unionpay"};
        String[] appNames = {getString(R.string.app_wechat), getString(R.string.app_alipay),
                getString(R.string.app_pinduoduo), getString(R.string.app_jingdong), getString(R.string.app_unionpay)};

        for (int i = 0; i < packages.length; i++) {
            Set<String> expense = KeywordManager.getKeywords(this, packages[i], KeywordManager.TYPE_EXPENSE);
            Set<String> income = KeywordManager.getKeywords(this, packages[i], KeywordManager.TYPE_INCOME);
            for (String kw : expense) {
                dataList.add(new KeywordItem(packages[i], appNames[i], kw, 0));
            }
            for (String kw : income) {
                dataList.add(new KeywordItem(packages[i], appNames[i], kw, 1));
            }
        }
        Collections.sort(dataList);
        adapter.notifyDataSetChanged();
    }

    private void showAddKeywordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_keyword, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        android.widget.Spinner spApp = view.findViewById(R.id.sp_app);
        android.widget.EditText etKeyword = view.findViewById(R.id.et_keyword);
        android.widget.RadioGroup rgType = view.findViewById(R.id.rg_type);

        String[] appLabels = {getString(R.string.app_wechat), getString(R.string.app_alipay),
                getString(R.string.app_pinduoduo), getString(R.string.app_jingdong), getString(R.string.app_unionpay)};
        String[] appPackages = {"com.tencent.mm", "com.eg.android.AlipayGphone",
                "com.xunmeng.pinduoduo", "com.jingdong.app.mall", "com.unionpay"};
        android.widget.ArrayAdapter<String> spAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, appLabels);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spApp.setAdapter(spAdapter);

        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String kw = etKeyword.getText().toString().trim();
            if (kw.isEmpty()) {
                Toast.makeText(this, R.string.enter_keyword, Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spApp.getSelectedItemPosition();
            String pkg = appPackages[pos];
            int type = rgType.getCheckedRadioButtonId() == R.id.rb_income ? 1 : 0;
            Set<String> keywords = KeywordManager.getKeywords(this, pkg, type);
            keywords.add(kw);
            KeywordManager.saveKeywords(this, pkg, type, keywords);
            loadData();
            dialog.dismiss();
            Toast.makeText(this, R.string.keyword_added, Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_bottom_sheet_rounded);
        }
        dialog.show();
    }

    private class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.VH> {
        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            KeywordItem item = dataList.get(pos);
            h.tv1.setText(item.appName + ". " + (item.type == 1 ? getString(R.string.income_label) : getString(R.string.expense_label)) + ": " + item.text);

            h.itemView.setOnClickListener(v -> showEditKeywordDialog(item, pos));

            h.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(AssistantManagerActivity.this)
                        .setTitle(R.string.delete_keyword)
                        .setMessage(getString(R.string.delete_confirm, item.appName, item.text))
                        .setPositiveButton(R.string.delete_btn, (d, w) -> {
                            deleteKeyword(item, pos);
                        })
                        .setNegativeButton(R.string.cancel_btn, null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() { return dataList.size(); }

        class VH extends RecyclerView.ViewHolder {
            android.widget.TextView tv1;
            VH(View v) {
                super(v);
                tv1 = v.findViewById(android.R.id.text1);
                tv1.setTextSize(15);
                tv1.setTextColor(0xFF333333);
                tv1.setPadding(8, 8, 8, 8);
            }
        }
    }

    private void showEditKeywordDialog(KeywordItem item, int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_keyword, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        android.widget.Spinner spApp = view.findViewById(R.id.sp_app);
        android.widget.EditText etKeyword = view.findViewById(R.id.et_keyword);
        android.widget.RadioGroup rgType = view.findViewById(R.id.rg_type);

        String[] appLabels = {getString(R.string.app_wechat), getString(R.string.app_alipay),
                getString(R.string.app_pinduoduo), getString(R.string.app_jingdong), getString(R.string.app_unionpay)};
        String[] appPackages = {"com.tencent.mm", "com.eg.android.AlipayGphone",
                "com.xunmeng.pinduoduo", "com.jingdong.app.mall", "com.unionpay"};
        android.widget.ArrayAdapter<String> spAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, appLabels);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spApp.setAdapter(spAdapter);

        for (int i = 0; i < appPackages.length; i++) {
            if (appPackages[i].equals(item.packageName)) {
                spApp.setSelection(i);
                break;
            }
        }
        etKeyword.setText(item.text);
        if (item.type == 1) {
            rgType.check(R.id.rb_income);
        } else {
            rgType.check(R.id.rb_expense);
        }

        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String oldPkg = item.packageName;
            int oldType = item.type;
            String oldKw = item.text;

            String newKw = etKeyword.getText().toString().trim();
            if (newKw.isEmpty()) {
                Toast.makeText(this, R.string.enter_keyword, Toast.LENGTH_SHORT).show();
                return;
            }
            int idx = spApp.getSelectedItemPosition();
            String newPkg = appPackages[idx];
            int newType = rgType.getCheckedRadioButtonId() == R.id.rb_income ? 1 : 0;

            Set<String> oldKeywords = KeywordManager.getKeywords(this, oldPkg, oldType);
            oldKeywords.remove(oldKw);
            KeywordManager.saveKeywords(this, oldPkg, oldType, oldKeywords);

            Set<String> newKeywords = KeywordManager.getKeywords(this, newPkg, newType);
            newKeywords.add(newKw);
            KeywordManager.saveKeywords(this, newPkg, newType, newKeywords);

            loadData();
            dialog.dismiss();
            Toast.makeText(this, R.string.keyword_updated, Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_bottom_sheet_rounded);
        }
        dialog.show();
    }

    private void deleteKeyword(KeywordItem item, int unused) {
        Set<String> keywords = KeywordManager.getKeywords(this, item.packageName, item.type);
        keywords.remove(item.text);
        KeywordManager.saveKeywords(this, item.packageName, item.type, keywords);
        loadData();
        Toast.makeText(this, R.string.keyword_deleted, Toast.LENGTH_SHORT).show();
    }
}