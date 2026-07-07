package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationSettings {
    private static final String PREF_NAME = "notification_settings";
    private static final String KEY_BUDGET_NOTIFY_ENABLED = "budget_notify_enabled";
    private static final String KEY_BUDGET_WARNING_PERCENT = "budget_warning_percent";
    private static final String KEY_LAST_NOTIFIED_PERCENT = "last_notified_percent";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final long userId;

    public NotificationSettings(Context context, long userId) {
        this.userId = userId;
        prefs = context.getSharedPreferences(PREF_NAME + "_" + userId, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public boolean isBudgetNotifyEnabled() {
        return prefs.getBoolean(KEY_BUDGET_NOTIFY_ENABLED, true);
    }

    public void setBudgetNotifyEnabled(boolean enabled) {
        editor.putBoolean(KEY_BUDGET_NOTIFY_ENABLED, enabled);
        editor.apply();
    }

    public int getBudgetWarningPercent() {
        return prefs.getInt(KEY_BUDGET_WARNING_PERCENT, 80);
    }

    public void setBudgetWarningPercent(int percent) {
        editor.putInt(KEY_BUDGET_WARNING_PERCENT, percent);
        editor.apply();
    }

    public int getLastNotifiedPercent() {
        return prefs.getInt(KEY_LAST_NOTIFIED_PERCENT, 0);
    }

    public void setLastNotifiedPercent(int percent) {
        editor.putInt(KEY_LAST_NOTIFIED_PERCENT, percent);
        editor.apply();
    }

    public void resetLastNotified() {
        editor.putInt(KEY_LAST_NOTIFIED_PERCENT, 0);
        editor.apply();
    }
}