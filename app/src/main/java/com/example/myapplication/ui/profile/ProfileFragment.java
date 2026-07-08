package com.example.myapplication.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.myapplication.MyApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.NotificationSettings;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.data.repository.BillRepository;
import com.example.myapplication.ui.auth.ScalableVideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE_IMPORT_CSV = 1001;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1002;
    private static final String PREF_NAME = "profile_settings";
    private static final String KEY_AVATAR = "avatar_";

    private SessionManager sessionManager;
    private NotificationSettings notificationSettings;
    private BillRepository repository;
    private SharedPreferences profilePrefs;
    private ImageView ivAvatar;
    private SwitchCompat switchNotify;
    private ScalableVideoView profileVideo;
    private boolean isSettingSwitchProgrammatically;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveAvatar(uri);
                    Toast.makeText(requireContext(), R.string.profile_avatar_saved, Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        notificationSettings = new NotificationSettings(requireContext(), sessionManager.getUserId());
        repository = MyApplication.getRepository();
        profilePrefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        ivAvatar = view.findViewById(R.id.iv_avatar);
        switchNotify = view.findViewById(R.id.switch_notify);
        profileVideo = view.findViewById(R.id.profile_video);
        setupVideo();

        // Load saved avatar
        loadAvatar();

        // Avatar click -> pick from gallery
        view.findViewById(R.id.layout_avatar).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_CODE_NOTIFICATION_PERMISSION);
                    return;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_NOTIFICATION_PERMISSION);
                    return;
                }
            }
            pickImageLauncher.launch("image/*");
        });

        // Set notification switch state (no listener yet to avoid recursion)
        switchNotify.setChecked(notificationSettings.isBudgetNotifyEnabled());

        // Switch toggle -> directly save setting
        switchNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isSettingSwitchProgrammatically) return;
            notificationSettings.setBudgetNotifyEnabled(isChecked);
        });

        // Row click -> open detailed settings dialog
        view.findViewById(R.id.row_notify).setOnClickListener(v -> showNotificationSettings());

        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportCSV());
        view.findViewById(R.id.btn_import).setOnClickListener(v -> importCSV());
        view.findViewById(R.id.btn_download_template).setOnClickListener(v -> downloadCSVTemplate());

        View cacheRow = view.findViewById(R.id.row_cache);
        if (cacheRow != null) {
            cacheRow.setOnClickListener(v -> clearCache());
        }

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> logout());

        view.findViewById(R.id.btn_user_manage).setOnClickListener(v -> {
            long userId = sessionManager.getUserId();
            new Thread(() -> {
                com.example.myapplication.data.entity.User user = repository.getUserById(userId);
                if (user != null && user.isAdmin()) {
                    requireActivity().runOnUiThread(() -> {
                        startActivity(new Intent(requireContext(), com.example.myapplication.ui.auth.UserManageActivity.class));
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "只有管理员可以管理用户", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        return view;
    }

    private void setupVideo() {
        Uri videoUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.splash_bg);
        profileVideo.setVideoURI(videoUri);
        profileVideo.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });
        profileVideo.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (profileVideo != null && profileVideo.isPlaying()) {
            profileVideo.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (profileVideo != null && !profileVideo.isPlaying()) {
            profileVideo.start();
        }
    }

    // ---- Avatar ----

    private File getAvatarFile() {
        File dir = new File(requireContext().getFilesDir(), "avatars");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "user_" + sessionManager.getUserId() + ".jpg");
    }

    private void loadAvatar() {
        File file = getAvatarFile();
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                ivAvatar.setImageBitmap(bitmap);
            }
        }
    }

    private void saveAvatar(Uri sourceUri) {
        try {
            File targetFile = getAvatarFile();
            InputStream is = requireContext().getContentResolver().openInputStream(sourceUri);
            if (is == null) return;
            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) > 0) {
                fos.write(buffer, 0, n);
            }
            fos.close();
            is.close();

            // Save path marker in per-user SharedPreferences
            profilePrefs.edit()
                    .putBoolean(KEY_AVATAR + sessionManager.getUserId(), true)
                    .apply();

            // Reload from file
            loadAvatar();
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.profile_avatar_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ---- Notification ----

    private void showNotificationSettings() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_notification_settings, null);
        Switch switchBudget = dialogView.findViewById(R.id.switch_budget);
        EditText etPercent = dialogView.findViewById(R.id.et_percent);

        switchBudget.setChecked(notificationSettings.isBudgetNotifyEnabled());
        etPercent.setText(String.valueOf(notificationSettings.getBudgetWarningPercent()));

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("通知提醒设置")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    boolean enabled = switchBudget.isChecked();
                    int percent = 80;
                    try {
                        percent = Integer.parseInt(etPercent.getText().toString().trim());
                        if (percent < 10 || percent > 100) {
                            Toast.makeText(requireContext(), "提醒百分比应在10-100之间", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "请输入有效的百分比", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationSettings.setBudgetNotifyEnabled(enabled);
                    notificationSettings.setBudgetWarningPercent(percent);
                    notificationSettings.resetLastNotified();

                    syncSwitchState();
                    Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .setNeutralButton("功能说明", (dialog, which) -> showNotificationInfo())
                .show();
    }

    private void syncSwitchState() {
        isSettingSwitchProgrammatically = true;
        switchNotify.setChecked(notificationSettings.isBudgetNotifyEnabled());
        isSettingSwitchProgrammatically = false;
    }

    private void showNotificationInfo() {
        String message = "通知提醒功能说明：\n\n" +
                "1. 预算预警提醒\n" +
                "   当本月支出达到您设置的预算百分比时（默认80%），发送一条提醒通知，帮助您及时控制支出。\n\n" +
                "2. 预算超支警告\n" +
                "   当本月支出超过总预算时，发送高优先级警告通知，带振动提醒。\n\n" +
                "3. 智能防重复\n" +
                "   系统会记录已通知的百分比，避免重复打扰。每月1号自动重置通知状态。\n\n" +
                "4. 独立存储\n" +
                "   每个用户的通知设置独立保存，互不影响。";

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("通知功能说明")
                .setMessage(message)
                .setPositiveButton("知道了", null)
                .show();
    }

    // ---- CSV Export / Import ----

    private void exportCSV() {
        long userId = sessionManager.getUserId();
        LiveData<List<Bill>> liveBills = repository.getAllBills(userId);
        liveBills.observe(getViewLifecycleOwner(), bills -> {
            if (bills == null || bills.isEmpty()) {
                Toast.makeText(requireContext(), "暂无账单数据", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
            StringBuilder csv = new StringBuilder("日期,类型,分类,金额,备注\n");
            for (Bill b : bills) {
                csv.append(sdf.format(new Date(b.getTimestamp()))).append(",");
                csv.append("income".equals(b.getCategory()) ? "收入" : "支出").append(",");
                csv.append(b.getType()).append(",");
                csv.append(String.format(Locale.CHINA, "%.2f", Math.abs(b.getAmount()))).append(",");
                csv.append(b.getNote() != null ? b.getNote() : "").append("\n");
            }

            try {
                String fileName = "bills_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) + ".csv";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                        if (os != null) {
                            os.write(csv.toString().getBytes("UTF-8"));
                            os.close();
                            Toast.makeText(requireContext(), "已导出到 Download/" + fileName, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File file = new File(dir, fileName);
                    java.io.FileWriter fw = new java.io.FileWriter(file);
                    fw.write(csv.toString());
                    fw.close();
                    Toast.makeText(requireContext(), "已导出到 " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadCSVTemplate() {
        String templateContent = "日期,类型,分类,金额,备注\n" +
                "2025-01-01 12:00,支出,餐饮,50.00,午餐\n" +
                "2025-01-02 09:00,收入,工资,5000.00,月薪\n";

        try {
            String fileName = "bills_template.csv";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        os.write(templateContent.getBytes("UTF-8"));
                        os.close();
                        Toast.makeText(requireContext(), "模板已下载到 Download/" + fileName, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, fileName);
                java.io.FileWriter fw = new java.io.FileWriter(file);
                fw.write(templateContent);
                fw.close();
                Toast.makeText(requireContext(), "模板已下载到 " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "下载模板失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importCSV() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, REQUEST_CODE_IMPORT_CSV);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_CSV && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Intent previewIntent = new Intent(requireContext(), com.example.myapplication.ui.stats.CsvImportPreviewActivity.class);
                previewIntent.putExtra(com.example.myapplication.ui.stats.CsvImportPreviewActivity.EXTRA_CSV_URI, uri);
                startActivity(previewIntent);
            }
        }
    }

    private void clearCache() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("清除旧数据")
                .setMessage("此操作将删除上一年及更早的所有账单记录，只保留今年的数据。确定要清除吗？")
                .setPositiveButton("确定清除", (dialog, which) -> clearOldBills())
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearOldBills() {
        long userId = sessionManager.getUserId();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.set(currentYear, Calendar.JANUARY, 1, 0, 0, 0);
        long startTimeOfYear = calendar.getTimeInMillis();

        new Thread(() -> {
            try {
                int deletedCount = repository.deleteBillsBefore(userId, startTimeOfYear);
                notificationSettings.resetLastNotified();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "已清除 " + deletedCount + " 条旧记录", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "清除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(requireContext(), com.example.myapplication.ui.auth.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}