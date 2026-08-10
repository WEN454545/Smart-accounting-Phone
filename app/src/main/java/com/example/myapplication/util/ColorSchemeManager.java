package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.R;

/**
 * Manages color scheme selection and application.
 * Stores user preference in SharedPreferences and provides current color values.
 */
public final class ColorSchemeManager {

    private static final String PREF_NAME = "color_scheme_prefs";
    private static final String KEY_SCHEME = "selected_scheme";

    public static final String SCHEME_TEAL_SAKURA = "teal_sakura";
    public static final String SCHEME_LAVENDER_DREAM = "lavender_dream";
    public static final String SCHEME_OCEAN_MINT = "ocean_mint";

    private ColorSchemeManager() {}

    /** Get the currently selected scheme name, defaults to teal_sakura */
    public static String getSelectedScheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SCHEME, SCHEME_TEAL_SAKURA);
    }

    /** Save the selected scheme */
    public static void setSelectedScheme(Context context, String scheme) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SCHEME, scheme).apply();
    }

    /** Returns the theme resource ID for the current scheme */
    public static int getThemeResId(Context context) {
        return getThemeResId(getSelectedScheme(context));
    }

    /** Returns the theme resource ID for a given scheme */
    public static int getThemeResId(String scheme) {
        switch (scheme) {
            case SCHEME_LAVENDER_DREAM:
                return R.style.Theme_MyApplication_Lavender;
            case SCHEME_OCEAN_MINT:
                return R.style.Theme_MyApplication_Ocean;
            default:
                return R.style.Theme_MyApplication_Teal;
        }
    }

    /** Returns the primary color for the current scheme */
    public static int getCurrentPrimaryColor(Context context) {
        switch (getSelectedScheme(context)) {
            case SCHEME_LAVENDER_DREAM:
                return context.getColor(R.color.scheme_lavender_primary);
            case SCHEME_OCEAN_MINT:
                return context.getColor(R.color.scheme_ocean_primary);
            default:
                return context.getColor(R.color.primary);
        }
    }

    /** Returns the primary dark color for the current scheme */
    public static int getCurrentPrimaryDarkColor(Context context) {
        switch (getSelectedScheme(context)) {
            case SCHEME_LAVENDER_DREAM:
                return context.getColor(R.color.scheme_lavender_primary_dark);
            case SCHEME_OCEAN_MINT:
                return context.getColor(R.color.scheme_ocean_primary_dark);
            default:
                return context.getColor(R.color.primary_dark);
        }
    }

    /** Returns the accent color for the current scheme */
    public static int getCurrentAccentColor(Context context) {
        switch (getSelectedScheme(context)) {
            case SCHEME_LAVENDER_DREAM:
                return context.getColor(R.color.scheme_lavender_accent);
            case SCHEME_OCEAN_MINT:
                return context.getColor(R.color.scheme_ocean_accent);
            default:
                return context.getColor(R.color.anime_sakura);
        }
    }

    /** Returns the background color for the current scheme */
    public static int getCurrentBgColor(Context context) {
        switch (getSelectedScheme(context)) {
            case SCHEME_LAVENDER_DREAM:
                return context.getColor(R.color.scheme_lavender_bg);
            case SCHEME_OCEAN_MINT:
                return context.getColor(R.color.scheme_ocean_bg);
            default:
                return context.getColor(R.color.bg);
        }
    }

    /** Returns the display name of a scheme */
    public static String getSchemeDisplayName(String scheme) {
        switch (scheme) {
            case SCHEME_TEAL_SAKURA:
                return "\u9752\u74F7\u6A31";
            case SCHEME_LAVENDER_DREAM:
                return "\u68A6\u5E7B\u7D2B";
            case SCHEME_OCEAN_MINT:
                return "\u6D77\u6D0B\u8584\u8377";
            default:
                return "\u9752\u74F7\u6A31";
        }
    }

    /** Returns the description of a scheme */
    public static String getSchemeDescription(String scheme) {
        switch (scheme) {
            case SCHEME_TEAL_SAKURA:
                return "\u9752\u74F7\u7EFF \u00D7 \u6A31\u82B1\u7C89";
            case SCHEME_LAVENDER_DREAM:
                return "\u7D2B\u7F57\u5170 \u00D7 \u7C89\u7D2B";
            case SCHEME_OCEAN_MINT:
                return "\u6DF1\u6D77\u84DD \u00D7 \u8584\u8377\u7EFF";
            default:
                return "\u9752\u74F7\u7EFF \u00D7 \u6A31\u82B1\u7C89";
        }
    }

    /** Returns the color array for preview circles: [primary, primaryDark, accent, bg] */
    public static int[] getSchemePreviewColors(Context context, String scheme) {
        switch (scheme) {
            case SCHEME_LAVENDER_DREAM:
                return new int[] {
                    context.getColor(R.color.scheme_lavender_primary),
                    context.getColor(R.color.scheme_lavender_primary_dark),
                    context.getColor(R.color.scheme_lavender_accent),
                    context.getColor(R.color.scheme_lavender_bg)
                };
            case SCHEME_OCEAN_MINT:
                return new int[] {
                    context.getColor(R.color.scheme_ocean_primary),
                    context.getColor(R.color.scheme_ocean_primary_dark),
                    context.getColor(R.color.scheme_ocean_accent),
                    context.getColor(R.color.scheme_ocean_bg)
                };
            default: // teal_sakura
                return new int[] {
                    context.getColor(R.color.primary),
                    context.getColor(R.color.primary_dark),
                    context.getColor(R.color.anime_sakura),
                    context.getColor(R.color.bg)
                };
        }
    }
}