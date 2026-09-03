package com.example.myapplication.util;



import android.content.Context;

import android.graphics.drawable.GradientDrawable;

import android.util.TypedValue;

import android.view.View;

import android.widget.ImageView;

import android.widget.TextView;



import com.example.myapplication.R;



import java.util.LinkedHashMap;

import java.util.Map;



/**

 * Maps bill category names to drawable icon resource IDs.

 * Categories without an icon return 0 (callers fall back to emoji).

 *

 * How to add a new category icon:

 *   1. Drop a vector/PNG drawable named "ic_cat_xxx" into res/drawable (or drawable-nodpi).

 *   2. Register it in ICON_MAP below: ICON_MAP.put("\u5206\u7C7B\u540D", R.drawable.ic_cat_xxx);

 *   (Optionally also add it to CategoryManager.getAllAvailableIcons() so it appears in the icon picker.)

 */

public final class CategoryIconHelper {



    private static final float ICON_CORNER_RADIUS_DP = 12f;



    /** Category name (Chinese, Unicode-escaped) -> icon drawable resource ID. */

    private static final Map<String, Integer> ICON_MAP = new LinkedHashMap<>();



    static {
        // ===== Legacy default bindings (original 10 PNG icons, kept per product decision) =====
        ICON_MAP.put("\u9910\u996E", R.drawable.ic_cat_food);              // food
        ICON_MAP.put("\u8D2D\u7269", R.drawable.ic_cat_shopping);          // shopping
        ICON_MAP.put("\u4EA4\u901A", R.drawable.ic_cat_transport);         // transport
        ICON_MAP.put("\u65C5\u884C", R.drawable.ic_cat_travel);            // travel
        ICON_MAP.put("\u901A\u8BAF", R.drawable.ic_cat_communication);     // communication
        ICON_MAP.put("\u5A31\u4E50", R.drawable.ic_cat_entertainment);     // entertainment
        ICON_MAP.put("\u4EBA\u60C5", R.drawable.ic_cat_social);            // social
        ICON_MAP.put("\u533B\u7597", R.drawable.ic_cat_medical);           // medical
        ICON_MAP.put("\u6559\u80B2", R.drawable.ic_cat_education);         // education
        ICON_MAP.put("\u5176\u4ED6", R.drawable.ic_cat_other);             // other
        // NOTE: all newer vector icons (ic_cat_housing, ic_cat_pet, ...) are intentionally
        // NOT bound here. The icon library is a neutral pool: a category shows a custom
        // icon ONLY after the user explicitly picks one in CategorySettingsActivity
        // (stored via CategoryManager.setCategoryIcon). Everything else falls back to emoji.
    }



    private CategoryIconHelper() {}



    /**

     * Returns the drawable resource ID for the given category, or 0 if no icon exists.

     */

    public static int getIconResId(String type) {

        if (type == null) return 0;

        Integer resId = ICON_MAP.get(type);

        return resId != null ? resId : 0;

    }



    /**

     * Like {@link #getIconResId(String)} but honors the user's customized icon mapping

     * (set in CategorySettingsActivity) first, falling back to the built-in table.

     */

    public static int getIconResId(Context context, String type) {

        if (type == null) return 0;

        if (context != null) {

            String customName = com.example.autobookkeep.util.CategoryManager

                    .getCategoryIconName(context, type);

            if (customName != null && !customName.isEmpty()) {

                int resId = context.getResources().getIdentifier(

                        customName, "drawable", context.getPackageName());

                if (resId != 0) return resId;

            }

        }

        return getIconResId(type);

    }



    /**

     * Binds the appropriate icon to the provided views.

     * If a PNG icon exists for the category, shows the ImageView; otherwise shows the emoji TextView.

     *

     * @param emojiView  TextView used for emoji fallback

     * @param imageView  ImageView used for PNG icons

     * @param type       category name

     * @param emoji      emoji string (used when no PNG icon exists)

     * @param bgColorRes background color resource ID for the icon circle

     * @param context    context for resolving color

     */

    public static void bindIcon(TextView emojiView, ImageView imageView,

                                String type, String emoji, int bgColorRes, Context context) {

        int iconResId = getIconResId(context, type);

        int bgColor = context.getColor(bgColorRes);

        if (iconResId != 0) {

            imageView.setImageResource(iconResId);

            imageView.setBackground(createRoundedBg(bgColor, context));

            imageView.setVisibility(View.VISIBLE);

            emojiView.setVisibility(View.GONE);

        } else {

            emojiView.setText(emoji);

            emojiView.setBackground(createRoundedBg(bgColor, context));

            emojiView.setVisibility(View.VISIBLE);

            imageView.setVisibility(View.GONE);

        }

    }



    private static GradientDrawable createRoundedBg(int color, Context context) {

        GradientDrawable drawable = new GradientDrawable();

        drawable.setShape(GradientDrawable.RECTANGLE);

        drawable.setCornerRadius(TypedValue.applyDimension(

                TypedValue.COMPLEX_UNIT_DIP, ICON_CORNER_RADIUS_DP,

                context.getResources().getDisplayMetrics()));

        drawable.setColor(color);

        return drawable;

    }

}