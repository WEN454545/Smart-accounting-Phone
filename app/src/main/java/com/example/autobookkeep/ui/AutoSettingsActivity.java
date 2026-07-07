package com.example.autobookkeep.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myapplication.R;
import com.example.autobookkeep.util.AutoTrackLogManager;

/**
 * 自动记账设置中间页
 */
public class AutoSettingsActivity extends AppCompatActivity {

    private SwitchCompat switchLogCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_settings);

        switchLogCapture = findViewById(R.id.switchLogCapture);

        // 初始化日志开关状态
        boolean logEnabled = AutoTrackLogManager.isLogEnabled(this);
        switchLogCapture.setChecked(logEnabled);

        // 日志开关切换
        switchLogCapture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AutoTrackLogManager.setLogEnabled(this, isChecked);
            Toast.makeText(this, isChecked ? R.string.log_capture_enabled : R.string.log_capture_disabled, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.card_category).setOnClickListener(v -> {
            startActivity(new Intent(this, CategorySettingsActivity.class));
        });

        findViewById(R.id.card_assistant).setOnClickListener(v -> {
            startActivity(new Intent(this, AssistantManagerActivity.class));
        });

        findViewById(R.id.card_budget).setOnClickListener(v -> {
            startActivity(new Intent(this, BudgetSettingsActivity.class));
        });

        // 查看日志入口：只有开启日志捕获后才能查看
        findViewById(R.id.layout_log_entry).setOnClickListener(v -> {
            if (AutoTrackLogManager.isLogEnabled(this)) {
                startActivity(new Intent(this, AutoTrackLogActivity.class));
            } else {
                Toast.makeText(this, R.string.log_view_need_enable, Toast.LENGTH_SHORT).show();
            }
        });
    }
}