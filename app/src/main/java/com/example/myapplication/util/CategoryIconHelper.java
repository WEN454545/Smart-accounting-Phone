package com.example.myapplication.util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

/**
 * Maps bill category names to drawable icon resource IDs.
 * Categories without a PNG icon return 0 (callers fall back to emoji).
 */
public final class CategoryIconHelper {

    private CategoryIconHelper() {}

    /**
     * Returns the drawable resource ID for the given category, or 0 if no PNG icon exists.
     */
    public static int getIconResId(String type) {
        if (type == null) return 0;
        switch (type) {
            case "\u9910\u996E": return R.drawable.ic_cat_food;        // food
            case "\u8D2D\u7269": return R.drawable.ic_cat_shopping;    // shopping
            case "\u4EA4\u901A": return R.drawable.ic_cat_transport;   // transport
            case "\u65C5\u884C": return R.drawable.ic_cat_travel;      // travel
            case "\u901A\u8BAF": return R.drawable.ic_cat_communication; // communication
            case "\u5A31\u4E50": return R.drawable.ic_cat_entertainment; // entertainment
            case "\u4EBA\u60C5": return R.drawable.ic_cat_social;      // social
            case "\u533B\u7597": return R.drawable.ic_cat_medical;     // medical
            case "\u6559\u80B2": return R.drawable.ic_cat_education;   // education
            case "\u5176\u4ED6": return R.drawable.ic_cat_other;       // other
            default: return 0;
        }
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
        int iconResId = getIconResId(type);
        int bgColor = context.getColor(bgColorRes);
        if (iconResId != 0) {
            imageView.setImageResource(iconResId);
            imageView.setBackgroundColor(bgColor);
            imageView.setVisibility(View.VISIBLE);
            emojiView.setVisibility(View.GONE);
        } else {
            emojiView.setText(emoji);
            emojiView.setBackgroundColor(bgColor);
            emojiView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
        }
    }
}
