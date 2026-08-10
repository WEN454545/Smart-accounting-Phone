package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.data.SessionManager;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.ui.calendar.CalendarFragment;
import com.example.myapplication.ui.home.HomeFragment;
import com.example.myapplication.ui.profile.ProfileFragment;
import com.example.myapplication.ui.stats.StatsFragment;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ImageView ivGlobalBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MyApplication.getThemeResId());
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        ivGlobalBg = findViewById(R.id.iv_global_bg);
        loadGlobalBackground();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_calendar) {
                selected = new CalendarFragment();
            } else if (id == R.id.nav_stats) {
                selected = new StatsFragment();
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGlobalBackground();
    }

    private void loadGlobalBackground() {
        SharedPreferences prefs = getSharedPreferences("profile_settings", Context.MODE_PRIVATE);
        String savedPath = prefs.getString("home_bg_uri", null);
        if (savedPath != null) {
            try {
                if (savedPath.startsWith("content://")) {
                    Uri uri = Uri.parse(savedPath);
                    ivGlobalBg.setImageURI(uri);
                } else {
                    ivGlobalBg.setImageURI(Uri.fromFile(new java.io.File(savedPath)));
                }
                ivGlobalBg.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivGlobalBg.setVisibility(View.GONE);
            }
        } else {
            ivGlobalBg.setVisibility(View.GONE);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String serviceName = SelectToSpeakService.class.getName();
        android.view.accessibility.AccessibilityManager am =
                (android.view.accessibility.AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<android.accessibilityservice.AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (android.accessibilityservice.AccessibilityServiceInfo service : enabledServices) {
            if (serviceName.equals(service.getId())) {
                return true;
            }
        }
        return false;
    }

    private void checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.main_accessibility_title)
                    .setMessage(R.string.main_accessibility_message)
                    .setPositiveButton(R.string.main_go_settings, (d, w) -> {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.main_overlay_title)
                    .setMessage(R.string.main_overlay_message)
                    .setPositiveButton(R.string.main_go_settings, (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}