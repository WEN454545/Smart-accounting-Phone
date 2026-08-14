package com.example.autobookkeep.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AssistantConfig {
    private static final String PREF_NAME = "autobookkeep_config";

    private static final String KEY_ENABLE = "key_enable_auto_track";
    private static final String KEY_AUTO_TRACK_USER_ID = "key_auto_track_user_id";
    private static final String KEY_ENABLE_VOICE_ANNOUNCE = "key_enable_voice_announce";
    private static final String KEY_VOICE_TYPE = "key_voice_type";
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

    public long getAutoTrackUserId() {
        return prefs.getLong(KEY_AUTO_TRACK_USER_ID, -1);
    }

    public void setAutoTrackUserId(long userId) {
        prefs.edit().putLong(KEY_AUTO_TRACK_USER_ID, userId).apply();
    }

    public boolean isVoiceAnnounceEnabled() {
        return prefs.getBoolean(KEY_ENABLE_VOICE_ANNOUNCE, false);
    }

    public void setVoiceAnnounceEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_VOICE_ANNOUNCE, enabled).apply();
    }

    // 0 = system TTS, 1 = Citlali-style TTS
    public int getVoiceType() {
        return prefs.getInt(KEY_VOICE_TYPE, 0);
    }

    public void setVoiceType(int type) {
        prefs.edit().putInt(KEY_VOICE_TYPE, type).apply();
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

    public void setCurrencyEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_CURRENCY, enabled).apply();
    }

    public boolean isPhotoBackupEnabled() {
        return prefs.getBoolean(KEY_ENABLE_PHOTO, false);
    }
}