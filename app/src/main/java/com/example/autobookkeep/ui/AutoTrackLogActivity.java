package com.example.autobookkeep.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.autobookkeep.util.AutoTrackLogManager;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动记账适配日志查看页
 */
public class AutoTrackLogActivity extends AppCompatActivity {
    private RecyclerView rvLogs;
    private Spinner spinnerPackage;
    private Switch switchCapture;
    private LogAdapter adapter;
    private String currentFilter = "全部";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_auto_track_log);

        View rootLayout = findViewById(R.id.root_layout);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            int padding16 = (int) (16 * getResources().getDisplayMetrics().density);
            v.setPadding(
                    systemBars.left + padding16,
                    systemBars.top + padding16,
                    systemBars.right + padding16,
                    systemBars.bottom + padding16
            );
            return insets;
        });

        rvLogs = findViewById(R.id.rv_logs);
        spinnerPackage = findViewById(R.id.spinner_package);
        switchCapture = findViewById(R.id.switch_capture);

        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            AutoTrackLogManager.clearLogs(this);
            currentFilter = "全部";
            refreshData();
        });

        findViewById(R.id.btn_copy).setOnClickListener(v -> copyCurrentLogs());

        boolean logEnabled = AutoTrackLogManager.isLogEnabled(this);
        switchCapture.setChecked(logEnabled);
        switchCapture.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (event.getAction() != android.view.MotionEvent.ACTION_UP) return false;
            boolean currentState = switchCapture.isChecked();
            boolean newState = !currentState;
            if (newState && !isAccessibilityServiceEnabled()) {
                showAccessibilityPermissionDialog();
                return true;
            }
            switchCapture.setChecked(newState);
            AutoTrackLogManager.setLogEnabled(this, newState);
            return true;
        });

        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter();
        rvLogs.setAdapter(adapter);

        setupSpinner();

        AutoTrackLogManager.setObserver(() -> runOnUiThread(this::refreshData));
        refreshData();
    }

    private static class AppSpinnerItem {
        String packageName;
        String appName;

        AppSpinnerItem(String packageName, String appName) {
            this.packageName = packageName;
            this.appName = appName;
        }

        @Override
        public String toString() {
            return appName;
        }
    }

    private static final java.util.Map<String, String> APP_NAME_MAP = new java.util.HashMap<String, String>() {{
        put("com.tencent.mm", "微信");
        put("com.eg.android.AlipayGphone", "支付宝");
        put("com.xunmeng.pinduoduo", "拼多多");
        put("com.jingdong.app.mall", "京东");
        put("com.sankuai.meituan", "美团");
        put("com.ss.android.ugc.aweme", "抖音");
        put("com.unionpay", "云闪付");
        put("com.alibaba.tongyi", "通义千问");
        put("com.taobao.taobao", "淘宝");
        put("com.doushengsheng.app", "抖省省");
    }};

    private String getAppName(String packageName) {
        if ("全部".equals(packageName)) return "全部应用";
        if (APP_NAME_MAP.containsKey(packageName)) return APP_NAME_MAP.get(packageName);
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AutoTrackLogManager.setObserver(null);
    }

    private void setupSpinner() {
        ArrayAdapter<AppSpinnerItem> spinnerAdapter = new ArrayAdapter<AppSpinnerItem>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                boolean isNightMode = (getContext().getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
                tv.setTextColor(isNightMode ? 0xFFDDDDDD : 0xFF333333);
                tv.setTextSize(15f);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                boolean isNightMode = (getContext().getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
                tv.setTextColor(isNightMode ? 0xFFDDDDDD : 0xFF333333);
                tv.setPadding(32, 32, 32, 32);
                return tv;
            }
        };
        spinnerPackage.setAdapter(spinnerAdapter);

        spinnerPackage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSpinnerItem selectedItem = (AppSpinnerItem) parent.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.packageName.equals(currentFilter)) {
                    currentFilter = selectedItem.packageName;
                    refreshData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshData() {
        List<String> rawPackages = AutoTrackLogManager.getPackages(this);
        List<AppSpinnerItem> newItems = new ArrayList<>();
        for (String pkg : rawPackages) {
            newItems.add(new AppSpinnerItem(pkg, getAppName(pkg)));
        }

        ArrayAdapter<AppSpinnerItem> spinnerAdapter = (ArrayAdapter<AppSpinnerItem>) spinnerPackage.getAdapter();

        boolean needsUpdate = false;
        if (spinnerAdapter.getCount() != newItems.size()) {
            needsUpdate = true;
        } else {
            for (int i = 0; i < newItems.size(); i++) {
                if (!spinnerAdapter.getItem(i).packageName.equals(newItems.get(i).packageName)) {
                    needsUpdate = true;
                    break;
                }
            }
        }

        if (needsUpdate) {
            spinnerAdapter.clear();
            spinnerAdapter.addAll(newItems);
            spinnerAdapter.notifyDataSetChanged();

            int selIndex = 0;
            for (int i = 0; i < newItems.size(); i++) {
                if (newItems.get(i).packageName.equals(currentFilter)) {
                    selIndex = i;
                    break;
                }
            }
            spinnerPackage.setSelection(selIndex);
        }

        List<AutoTrackLogManager.LogEntry> logs = AutoTrackLogManager.getLogs(this, currentFilter);
        adapter.setLogs(logs);
    }

    private void copyCurrentLogs() {
        List<AutoTrackLogManager.LogEntry> logs = AutoTrackLogManager.getLogs(this, currentFilter);
        StringBuilder sb = new StringBuilder();
        for (AutoTrackLogManager.LogEntry log : logs) {
            sb.append(log.time).append(" [").append(log.packageName).append("] ").append(log.message).append("\n");
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("适配日志", sb.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "已复制 " + logs.size() + " 条日志", Toast.LENGTH_SHORT).show();
    }

    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.VH> {
        private List<AutoTrackLogManager.LogEntry> logs = new ArrayList<>();

        void setLogs(List<AutoTrackLogManager.LogEntry> newLogs) {
            this.logs = newLogs;
            notifyDataSetChanged();
            if (!logs.isEmpty()) {
                rvLogs.postDelayed(() -> rvLogs.scrollToPosition(logs.size() - 1), 100);
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            AutoTrackLogManager.LogEntry log = logs.get(pos);
            h.tv1.setText(log.time + " [" + log.packageName + "]");
            h.tv2.setText(log.message);
        }

        @Override
        public int getItemCount() { return logs.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            VH(View v) {
                super(v);
                tv1 = v.findViewById(android.R.id.text1);
                tv2 = v.findViewById(android.R.id.text2);
                tv1.setTextSize(12);
                tv1.setTextColor(0xFF888888);
                tv2.setTextSize(13);
                tv2.setTextColor(0xFF333333);
            }
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String serviceName = SelectToSpeakService.class.getName();
        android.view.accessibility.AccessibilityManager am =
                (android.view.accessibility.AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        java.util.List<android.accessibilityservice.AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (android.accessibilityservice.AccessibilityServiceInfo service : enabledServices) {
            if (serviceName.equals(service.getId())) {
                return true;
            }
        }
        return false;
    }

    private void showAccessibilityPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("无障碍服务未开启")
                .setMessage("节点抓取依赖无障碍服务，请在系统设置中开启「智能记账」的无障碍服务")
                .setPositiveButton("去设置", (d, w) -> {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                })
                .setCancelable(false)
                .show();
    }
}