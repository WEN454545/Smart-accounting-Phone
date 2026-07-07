package com.example.autobookkeep.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoAssetManager {

    private static final String PREF_NAME = "auto_asset_prefs";
    private static final String KEY_ENABLE = "key_enable_auto_asset";
    private static final String KEY_RULES = "key_auto_asset_rules";

    public static class AssetRule {
        public String packageName;
        public String keyword;
        public int assetId;

        public AssetRule(String packageName, String keyword, int assetId) {
            this.packageName = packageName;
            this.keyword = keyword;
            this.assetId = assetId;
        }

        @Override
        public String toString() {
            return packageName + "|" + keyword + "|" + assetId;
        }

        public static AssetRule fromString(String str) {
            String[] parts = str.split("\\|");
            if (parts.length == 3) {
                try {
                    return new AssetRule(parts[0], parts[1], Integer.parseInt(parts[2]));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
    }

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLE, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ENABLE, enabled).apply();
    }

    public static int matchAsset(Context context, String packageName, String text) {
        if (!isEnabled(context)) return -1;
        if (text == null) return -1;
        List<AssetRule> rules = getRules(context);
        for (AssetRule rule : rules) {
            if (rule.packageName.equals(packageName) && text.contains(rule.keyword)) {
                return rule.assetId;
            }
        }
        return -1;
    }

    public static List<AssetRule> getRules(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(KEY_RULES, new HashSet<>());
        List<AssetRule> rules = new ArrayList<>();
        for (String s : set) {
            AssetRule rule = AssetRule.fromString(s);
            if (rule != null) rules.add(rule);
        }
        return rules;
    }

    public static void addRule(Context context, AssetRule rule) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_RULES, new HashSet<>()));
        set.add(rule.toString());
        prefs.edit().putStringSet(KEY_RULES, set).apply();
    }
}