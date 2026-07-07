package com.example.autobookkeep.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryManager {
    private static final String PREF_NAME = "category_prefs";
    private static final String KEY_EXPENSE = "key_expense_categories";
    private static final String KEY_INCOME = "key_income_categories";
    private static final String KEY_ENABLE_SUB_CATEGORY = "enable_sub_category";

    private static final String DEFAULT_EXPENSE = "餐饮,购物,住房,交通,旅行,通讯,娱乐,人情,医疗,教育,美容,其他";
    private static final String DEFAULT_INCOME = "转账,红包,退款,其他";

    public static List<String> getExpenseCategories(Context context) {
        return getList(context, KEY_EXPENSE, DEFAULT_EXPENSE);
    }

    public static List<String> getIncomeCategories(Context context) {
        return getList(context, KEY_INCOME, DEFAULT_INCOME);
    }

    public static void saveExpenseCategories(Context context, List<String> list) {
        saveList(context, KEY_EXPENSE, list);
    }

    public static void saveIncomeCategories(Context context, List<String> list) {
        saveList(context, KEY_INCOME, list);
    }

    public static boolean isSubCategoryEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLE_SUB_CATEGORY, false);
    }

    public static void setSubCategoryEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLE_SUB_CATEGORY, enabled).apply();
    }

    public static List<String> getSubCategories(Context context, String parentCategory) {
        return getList(context, "sub_cat_" + parentCategory, "");
    }

    public static void saveSubCategories(Context context, String parentCategory, List<String> list) {
        saveList(context, "sub_cat_" + parentCategory, list);
    }

    private static List<String> getList(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedString = prefs.getString(key, defaultValue);
        if (savedString == null || savedString.isEmpty()) {
            return new ArrayList<>();
        }
        String[] array = savedString.split(",");
        return new ArrayList<>(Arrays.asList(array));
    }

    private static void saveList(Context context, String key, List<String> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String joinedString = TextUtils.join(",", list);
        prefs.edit().putString(key, joinedString).apply();
    }

    // 详细分类开关
    private static final String KEY_ENABLE_DETAILED_CATEGORY = "enable_detailed_category";
    private static final String KEY_INITIALIZED_V2 = "initialized_v2";

    public static boolean isDetailedCategoryEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLE_DETAILED_CATEGORY, false);
    }

    public static void setDetailedCategoryEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLE_DETAILED_CATEGORY, enabled).apply();
    }

    /**
     * 强制写入默认分类（覆盖已有数据），分类设置页打开时调用
     */
    public static void initDefaults(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_INITIALIZED_V2, false)) {
            prefs.edit().putString(KEY_EXPENSE, DEFAULT_EXPENSE)
                        .putString(KEY_INCOME, DEFAULT_INCOME)
                        .putBoolean(KEY_INITIALIZED_V2, true)
                        .apply();
        }
    }

    /**
     * 强制重置为默认分类（覆盖已有数据）
     */
    public static void resetToDefaults(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_EXPENSE, DEFAULT_EXPENSE)
                    .putString(KEY_INCOME, DEFAULT_INCOME)
                    .apply();
    }
}