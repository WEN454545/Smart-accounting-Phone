package com.example.autobookkeep;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.google.android.accessibility.selecttospeak.SelectToSpeakService;

/**
 * 权限工具类 — 使用 Settings.Secure 字符串匹配，市面上最成熟的方式
 */
public final class PermissionUtils {

    private PermissionUtils() {}

    /**
     * 判断无障碍服务是否已开启（业界标准方法：读 ENABLED_ACCESSIBILITY_SERVICES 字符串匹配）
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        try {
            int enabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            if (enabled != 1) return false;

            String services = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services == null) return false;

            // 构建完整服务标识: com.example.myapplication/com.google.android.accessibility.selecttospeak.SelectToSpeakService
            String fullName = context.getPackageName() + "/" + SelectToSpeakService.class.getCanonicalName();
            if (services.contains(fullName)) return true;

            // 兼容某些 ROM 的扁平格式
            if (services.contains("selecttospeak") || services.contains("SelectToSpeak")) return true;

        } catch (Exception ignored) {}
        return false;
    }

    /**
     * 跳转系统无障碍设置
     */
    public static void openAccessibilitySettings(Context context) {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    /**
     * 判断悬浮窗权限是否已开启
     */
    public static boolean isOverlayPermissionEnabled(Context context) {
        return Settings.canDrawOverlays(context);
    }

    /**
     * 跳转悬浮窗权限设置
     */
    public static void openOverlaySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }
}