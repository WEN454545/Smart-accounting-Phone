package com.example.autobookkeep.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeywordManager {

    private static final String PREF_NAME = "autobookkeep_keywords";

    public static final int TYPE_EXPENSE = 0;
    public static final int TYPE_INCOME = 1;

    public static Set<String> getKeywords(Context context, String packageName, int type) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = "keywords_" + packageName + "_" + type;
        return prefs.getStringSet(key, new HashSet<>());
    }

    public static void addKeyword(Context context, String packageName, int type, String keyword) {
        Set<String> current = new HashSet<>(getKeywords(context, packageName, type));
        current.add(keyword);
        saveKeywords(context, packageName, type, current);
    }

    public static void removeKeyword(Context context, String packageName, int type, String keyword) {
        Set<String> current = new HashSet<>(getKeywords(context, packageName, type));
        current.remove(keyword);
        saveKeywords(context, packageName, type, current);
    }

    public static void saveKeywords(Context context, String packageName, int type, Set<String> newKeywords) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = "keywords_" + packageName + "_" + type;
        prefs.edit().putStringSet(key, newKeywords).apply();
    }

    public static void initDefaults(Context context) {
        // 不再预置任何关键字，全部由用户自行添加
    }

    public static Map<String, Set<String>> getAllKeywords(Context context, int type) {
        Map<String, Set<String>> result = new HashMap<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("keywords_") && key.endsWith("_" + type)) {
                String pkg = key.substring(9, key.length() - 2);
                Set<String> kw = prefs.getStringSet(key, new HashSet<>());
                if (!kw.isEmpty()) {
                    result.put(pkg, kw);
                }
            }
        }
        return result;
    }
}