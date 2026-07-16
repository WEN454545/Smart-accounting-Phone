package com.example.myapplication.data.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.NotificationSettings;

public class BudgetNotificationHelper {

    private static final String CHANNEL_ID = "budget_alert";
    private static final int NOTIFICATION_WARNING_ID = 1001;
    private static final int NOTIFICATION_OVER_ID = 1002;

    private final Context context;
    private final NotificationSettings settings;

    public BudgetNotificationHelper(Context context, long userId) {
        this.context = context;
        this.settings = new NotificationSettings(context, userId);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "\u9884\u7B97\u63D0\u9192";
            String description = "\u5F53\u652F\u51FA\u8D85\u8FC7\u9884\u7B97\u65F6\u53D1\u9001\u63D0\u9192";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void checkAndNotify(double expense, double budget) {
        if (!settings.isBudgetNotifyEnabled() || budget <= 0) {
            return;
        }

        int currentPercent = (int) ((expense / budget) * 100);
        int warningPercent = settings.getBudgetWarningPercent();
        int lastNotified = settings.getLastNotifiedPercent();

        if (expense > budget && lastNotified < 100) {
            showBudgetOverWarning(expense, budget);
            settings.setLastNotifiedPercent(100);
        } else if (currentPercent >= warningPercent && lastNotified < warningPercent) {
            showBudgetWarning(expense, budget, currentPercent);
            settings.setLastNotifiedPercent(currentPercent);
        }
    }

    public void showBudgetWarning(double expense, double budget, int percent) {
        String title = "\u9884\u7B97\u63D0\u9192";
        String message = "\u672C\u6708\u652F\u51FA\u5DF2\u8FBE\u9884\u7B97\u7684 " + percent + "%\uFF08\u00A5" + String.format("%.2f", expense) +
                " / \u00A5" + String.format("%.2f", budget) + "\uFF09\uFF0C\u8BF7\u6CE8\u610F\u63A7\u5236\u652F\u51FA\u3002";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 200, 300});

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_WARNING_ID, builder.build());
        } catch (SecurityException e) {
        }
    }

    public void showBudgetOverWarning(double expense, double budget) {
        double overAmount = expense - budget;
        String title = "\u9884\u7B97\u8D85\u652F\u8B66\u544A";
        String message = "\u672C\u6708\u652F\u51FA\u5DF2\u8D85\u51FA\u9884\u7B97 \u00A5" + String.format("%.2f", overAmount) +
                "\uFF01\u5F53\u524D\u652F\u51FA\uFF1A\u00A5" + String.format("%.2f", expense) +
                "\uFF0C\u9884\u7B97\uFF1A\u00A5" + String.format("%.2f", budget);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 300, 500, 300, 500});

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_OVER_ID, builder.build());
        } catch (SecurityException e) {
        }
    }

    public static boolean shouldNotify(double expense, double budget, int warningPercent) {
        if (budget <= 0) return false;
        int percent = (int) ((expense / budget) * 100);
        return percent >= warningPercent;
    }

    public static boolean isOverBudget(double expense, double budget) {
        return expense > budget;
    }
}