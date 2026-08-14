package com.example.autobookkeep.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.myapplication.util.CategoryIconHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // ===== 分类图标自定义映射 =====
    private static final String KEY_CATEGORY_ICON_MAP = "key_category_icon_map";

    /**
     * 获取分类的自定义图标资源名，无自定义返回 null
     */
    public static String getCategoryIconName(Context context, String categoryName) {
        Map<String, String> map = getIconMapping(context);
        return map.get(categoryName);
    }

    /**
     * 设置分类的自定义图标
     * @param iconResName 图标资源名（如 "ic_cat_food"），传 null 或空字符串表示恢复默认
     */
    public static void setCategoryIcon(Context context, String categoryName, String iconResName) {
        Map<String, String> map = getIconMapping(context);
        if (iconResName == null || iconResName.isEmpty()) {
            map.remove(categoryName);
        } else {
            map.put(categoryName, iconResName);
        }
        saveIconMapping(context, map);
    }

    private static Map<String, String> getIconMapping(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_CATEGORY_ICON_MAP, "");
        Map<String, String> map = new LinkedHashMap<>();
        if (saved == null || saved.isEmpty()) return map;
        String[] entries = saved.split("\\|");
        for (String entry : entries) {
            String[] kv = entry.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    private static void saveIconMapping(Context context, Map<String, String> map) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (map.isEmpty()) {
            prefs.edit().putString(KEY_CATEGORY_ICON_MAP, "").apply();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        prefs.edit().putString(KEY_CATEGORY_ICON_MAP, sb.toString()).apply();
    }

    // ===== 可用图标列表 =====
    public static class IconInfo {
        public final int resId;
        public final String resName;
        public final String label;

        IconInfo(int resId, String resName, String label) {
            this.resId = resId;
            this.resName = resName;
            this.label = label;
        }
    }

    /**
     * 获取所有可供选择的分类图标列表
     */
    public static List<IconInfo> getAllAvailableIcons(Context context) {
        List<IconInfo> list = new ArrayList<>();
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_food, "ic_cat_food", "餐饮"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_shopping, "ic_cat_shopping", "购物"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_transport, "ic_cat_transport", "交通"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_travel, "ic_cat_travel", "旅行"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_communication, "ic_cat_communication", "通讯"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_entertainment, "ic_cat_entertainment", "娱乐"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_social, "ic_cat_social", "人情"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_medical, "ic_cat_medical", "医疗"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_education, "ic_cat_education", "教育"));
        list.add(new IconInfo(com.example.myapplication.R.drawable.ic_cat_other, "ic_cat_other", "其他"));
        return list;
    }
}