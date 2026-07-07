package com.example.autobookkeep.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AssistantConfig {
    private static final String PREF_NAME = "autobookkeep_config";

    private static final String KEY_ENABLE = "key_enable_auto_track";
    private static final String KEY_ENABLE_ASSETS = "key_enable_assets_module";
    private static final String KEY_ENABLE_DETAILS = "key_enable_details_module";
    private static final String KEY_DEFAULT_ASSET_ID = "key_default_asset_id";
    private static final String KEY_DEFAULT_CURRENCY = "default_currency_symbol";
    private static final String KEY_ENABLE_CURRENCY = "enable_currency";
    private static final String KEY_ENABLE_PHOTO = "enable_photo_backup";

    private final SharedPreferences prefs;

    public AssistantConfig(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLE, false);
    }

    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE, enabled).apply();
    }

    public boolean isAssetsEnabled() {
        return prefs.getBoolean(KEY_ENABLE_ASSETS, false);
    }

    public void setAssetsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_ASSETS, enabled).apply();
    }

    public boolean isDetailsEnabled() {
        return prefs.getBoolean(KEY_ENABLE_DETAILS, false);
    }

    public void setDetailsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_DETAILS, enabled).apply();
    }

    public int getDefaultAssetId() {
        return prefs.getInt(KEY_DEFAULT_ASSET_ID, -1);
    }

    public void setDefaultAssetId(int id) {
        prefs.edit().putInt(KEY_DEFAULT_ASSET_ID, id).apply();
    }

    public String getDefaultCurrency() {
        return prefs.getString(KEY_DEFAULT_CURRENCY, "¥");
    }

    public boolean isCurrencyEnabled() {
        return prefs.getBoolean(KEY_ENABLE_CURRENCY, false);
    }

    public boolean isPhotoBackupEnabled() {
        return prefs.getBoolean(KEY_ENABLE_PHOTO, false);
    }
}