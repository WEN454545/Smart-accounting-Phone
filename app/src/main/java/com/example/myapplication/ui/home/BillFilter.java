package com.example.myapplication.ui.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.myapplication.data.entity.Bill;

/**
 * Immutable-by-convention filter state for the All Bills page.
 * Passed to {@link #apply(List, String, BillFilter)} together with the
 * search keyword to produce the displayed bill list.
 */
public class BillFilter {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_EXPENSE = 1;
    public static final int TYPE_INCOME = 2;

    public static final int RANGE_ALL = 0;
    public static final int RANGE_MONTH = 1;
    public static final int RANGE_YEAR = 2;
    public static final int RANGE_CUSTOM = 3;

    public int billType = TYPE_ALL;
    /** Selected category names (bill.type); empty set = all categories. */
    public final Set<String> categories = new HashSet<>();
    public int range = RANGE_ALL;
    /** Custom range start (millis, day 00:00); 0 = not set. */
    public long customStart;
    /** Custom range end (millis, day 23:59:59.999); 0 = not set. */
    public long customEnd;
    /** Min amount (inclusive, absolute value); null = not set. */
    public Double minAmount;
    /** Max amount (inclusive, absolute value); null = not set. */
    public Double maxAmount;

    public BillFilter copy() {
        BillFilter f = new BillFilter();
        f.billType = billType;
        f.categories.addAll(categories);
        f.range = range;
        f.customStart = customStart;
        f.customEnd = customEnd;
        f.minAmount = minAmount;
        f.maxAmount = maxAmount;
        return f;
    }

    /** True when any filter dimension is active (used to tint the filter button). */
    public boolean isActive() {
        return billType != TYPE_ALL || !categories.isEmpty() || range != RANGE_ALL
                || minAmount != null || maxAmount != null;
    }

    /**
     * Filter {@code src} by keyword (matches note + category name) and this filter state.
     * Preserves the source order (timestamp DESC).
     */
    public static List<Bill> apply(List<Bill> src, String keyword, BillFilter f) {
        List<Bill> out = new ArrayList<>();
        if (src == null || src.isEmpty()) return out;
        if (f == null) f = new BillFilter();
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.CHINA);
        long[] range = f.resolveRange();
        for (Bill b : src) {
            if (f.billType == TYPE_EXPENSE && !"expense".equals(b.getCategory())) continue;
            if (f.billType == TYPE_INCOME && !"income".equals(b.getCategory())) continue;
            if (!f.categories.isEmpty() && !f.categories.contains(b.getType())) continue;
            if (range[0] > 0 && b.getTimestamp() < range[0]) continue;
            if (range[1] > 0 && b.getTimestamp() > range[1]) continue;
            // Match the amount the user actually sees in the list (original currency)
            double amt = Math.abs(b.getDisplayAmount());
            if (f.minAmount != null && amt < f.minAmount) continue;
            if (f.maxAmount != null && amt > f.maxAmount) continue;
            if (!q.isEmpty()) {
                String note = b.getNote() == null ? "" : b.getNote().toLowerCase(Locale.CHINA);
                String type = b.getType() == null ? "" : b.getType().toLowerCase(Locale.CHINA);
                if (!note.contains(q) && !type.contains(q)) continue;
            }
            out.add(b);
        }
        return out;
    }

    /** Resolve the selected time range to {start, end} millis; 0 = unbounded. */
    private long[] resolveRange() {
        long start = 0;
        long end = 0;
        Calendar cal = Calendar.getInstance();
        switch (range) {
            case RANGE_MONTH:
                start = startOfDay(cal, 1);
                cal.add(Calendar.MONTH, 1);
                end = startOfDay(cal, 1) - 1; // last ms of this month
                break;
            case RANGE_YEAR:
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                start = startOfDay(cal, 1);
                cal.add(Calendar.YEAR, 1);
                end = startOfDay(cal, 1) - 1; // last ms of this year
                break;
            case RANGE_CUSTOM:
                start = customStart;
                end = customEnd;
                if (start > 0 && end > 0 && start > end) {
                    long tmp = start;
                    start = end;
                    end = tmp;
                }
                break;
            default:
                break;
        }
        return new long[]{start, end};
    }

    /** Set cal to the first millisecond of the given day of month and return it. */
    private static long startOfDay(Calendar cal, int dayOfMonth) {
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
