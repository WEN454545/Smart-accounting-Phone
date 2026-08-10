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
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
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
import com.example.myapplication.ui.crop.CropImageActivity;
import com.example.myapplication.util.CategoryIconHelper;
import com.example.myapplication.util.ColorSchemeManager;
import com.example.myapplication.util.ImageUtils;

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
    private static final String KEY_BG_URI = "bg_media_uri";
    private static final String KEY_BG_TYPE = "bg_media_type";
    private static final String KEY_CAL_BG_URI = "cal_bg_uri";
    private static final String KEY_LOGIN_BG_URI = "login_bg_uri";
    private static final String KEY_HOME_BG_URI = "home_bg_uri";
    private static final String KEY_DIALOG_BILL_BG_URI = "dialog_bill_bg_uri";
    private static final String KEY_HOME_HEADER_BG_URI = "home_header_bg_uri";
    public static final String KEY_TRANSACTION_STYLE = "transaction_style";
    public static final String STYLE_STANDARD = "standard";
    public static final String STYLE_ISLAND = "island";

    // Aspect ratios for each target control (width / height)
    private static final float ASPECT_HOME_HEADER = 2.7f;       // Wide header
    private static final float ASPECT_HOME_FULL = 0.5625f;      // 9:16 phone screen
    private static final float ASPECT_CALENDAR = 1.0f;          // Square-ish card
    private static final float ASPECT_LOGIN = 0.5625f;          // 9:16 phone screen
    private static final float ASPECT_PROFILE = 1.8f;           // Profile card (wide)
    private static final float ASPECT_DIALOG_BILL = 0.75f;      // 3:4 dialog

    private SessionManager sessionManager;
    private NotificationSettings notificationSettings;
    private BillRepository repository;
    private SharedPreferences profilePrefs;
    private ImageView ivAvatar;
    private SwitchCompat switchNotify;
    private ScalableVideoView profileVideo;
    private ImageView ivProfileBg;
    private TextView tvBgStatus;
    private TextView tvTransactionStyleValue;
    private boolean isSettingSwitchProgrammatically;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveAvatar(uri);
                    Toast.makeText(requireContext(), R.string.profile_avatar_saved, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleMediaPicked(uri);
                }
            });

    private final ActivityResultLauncher<String[]> pickCalBgLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleCalBgPicked(uri);
                }
            });

    private final ActivityResultLauncher<String[]> pickLoginBgLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleLoginBgPicked(uri);
                }
            });

    private final ActivityResultLauncher<String[]> pickHomeBgLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleHomeBgPicked(uri);
                }
            });

    private final ActivityResultLauncher<String[]> pickDialogBillBgLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleDialogBillBgPicked(uri);
                }
            });

    private final ActivityResultLauncher<String[]> pickHomeHeaderBgLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleHomeHeaderBgPicked(uri);
                }
            });

    // Crop result launcher - handles all crop scenarios
    private final ActivityResultLauncher<Intent> cropResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleCropResult(result.getData());
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
        ivProfileBg = view.findViewById(R.id.iv_profile_bg);
        tvBgStatus = view.findViewById(R.id.tv_bg_status);
        tvTransactionStyleValue = view.findViewById(R.id.tv_transaction_style_value);
        setupBackground();

        // Load saved avatar
        loadAvatar();

        // Update transaction style display
        updateTransactionStyleDisplay();

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

        view.findViewById(R.id.row_personalize).setOnClickListener(v -> showPersonalizeDialog());

        // Transaction style row click
        View rowTransactionStyle = view.findViewById(R.id.row_transaction_style);
        if (rowTransactionStyle != null) {
            rowTransactionStyle.setOnClickListener(v -> showTransactionStyleDialog());
        }

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

    private void setupBackground() {
        String savedUri = profilePrefs.getString(KEY_BG_URI, null);
        String savedType = profilePrefs.getString(KEY_BG_TYPE, null);
        if (savedUri != null && savedType != null) {
            applyBackgroundMedia(resolveStoredPath(savedUri), savedType);
        } else {
            playDefaultVideo();
        }
    }

    private void playDefaultVideo() {
        ivProfileBg.setVisibility(View.GONE);
        profileVideo.setVisibility(View.VISIBLE);
        Uri videoUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.splash_bg);
        profileVideo.setVideoURI(videoUri);
        profileVideo.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });
        profileVideo.start();
        tvBgStatus.setText(R.string.profile_bg_default);
    }

    private void showPersonalizeDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_personalize, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.opt_profile_bg).setOnClickListener(v -> {
            dialog.dismiss();
            showBgSettingDialog();
        });

        dialogView.findViewById(R.id.opt_login_bg).setOnClickListener(v -> {
            dialog.dismiss();
            showLoginBgSettingDialog();
        });

        dialogView.findViewById(R.id.opt_home_bg).setOnClickListener(v -> {
            dialog.dismiss();
            showHomeBgSettingDialog();
        });

        dialogView.findViewById(R.id.opt_dialog_bill_bg).setOnClickListener(v -> {
            dialog.dismiss();
            showDialogBillBgSettingDialog();
        });

        dialogView.findViewById(R.id.opt_cal_bg).setOnClickListener(v -> {
            dialog.dismiss();
            showCalBgSettingDialog();
        });

        dialogView.findViewById(R.id.opt_home_header_bg).setOnClickListener(v -> {
            dialog.dismiss();
            showHomeHeaderBgSettingDialog();
        });

        dialogView.findViewById(R.id.opt_color_scheme).setOnClickListener(v -> {
            dialog.dismiss();
            showColorSchemeDialog();
        });

        dialogView.findViewById(R.id.btn_personalize_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showBgSettingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bg_setting, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        // --- Preview ---
        ImageView ivPreview = dialogView.findViewById(R.id.iv_preview_bg);
        View placeholder = dialogView.findViewById(R.id.ll_preview_placeholder);

        String savedUri = profilePrefs.getString(KEY_BG_URI, null);
        String savedType = profilePrefs.getString(KEY_BG_TYPE, null);
        if (savedUri != null && savedType != null) {
            placeholder.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            if ("image".equals(savedType)) {
                ivPreview.setImageURI(resolveStoredPath(savedUri));
            } else {
                // For video, show a static thumbnail from ContentResolver
                ivPreview.setImageURI(resolveStoredPath(savedUri));
            }
        } else {
            placeholder.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
        }

        // --- Choose new background ---
        dialogView.findViewById(R.id.btn_choose_bg).setOnClickListener(v -> {
            dialog.dismiss();
            pickMediaLauncher.launch(new String[]{"image/*", "video/*"});
        });

        // --- Reset to default ---
        dialogView.findViewById(R.id.btn_reset_bg).setOnClickListener(v -> {
            dialog.dismiss();
            resetBackgroundToDefault();
        });

        // --- Cancel ---
        dialogView.findViewById(R.id.btn_cancel_bg).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Launch the crop activity with the given image URI and target aspect ratio.
     */
    private void launchCropActivity(Uri uri, float aspectRatio, String prefKey) {
        Intent intent = new Intent(requireContext(), CropImageActivity.class);
        intent.putExtra(CropImageActivity.EXTRA_IMAGE_URI, uri);
        intent.putExtra(CropImageActivity.EXTRA_ASPECT_RATIO, aspectRatio);
        intent.putExtra(CropImageActivity.EXTRA_PREF_KEY, prefKey);
        cropResultLauncher.launch(intent);
    }

    /**
     * Handle the crop result: save the cropped image path to SharedPreferences.
     */
    private void handleCropResult(Intent data) {
        String savedPath = data.getStringExtra(CropImageActivity.EXTRA_RESULT_PATH);
        String prefKey = data.getStringExtra(CropImageActivity.EXTRA_PREF_KEY);

        if (savedPath == null || prefKey == null) {
            Toast.makeText(requireContext(), "裁剪失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the cropped image file path to SharedPreferences
        profilePrefs.edit().putString(prefKey, savedPath).apply();

        // For profile background, also save the media type
        if (KEY_BG_URI.equals(prefKey)) {
            profilePrefs.edit().putString(KEY_BG_TYPE, "image").apply();
        }

        // Show success message based on the scenario
        String message;
        switch (prefKey) {
            case KEY_BG_URI:
                message = "个人主页背景已更新";
                break;
            case KEY_CAL_BG_URI:
                message = "日历背景已更新";
                break;
            case KEY_LOGIN_BG_URI:
                message = "登录页背景已更新";
                break;
            case KEY_HOME_BG_URI:
                message = "主页背景已更新";
                break;
            case KEY_DIALOG_BILL_BG_URI:
                message = "记账弹窗背景已更新";
                break;
            case KEY_HOME_HEADER_BG_URI:
                message = "主页顶部背景已更新";
                break;
            default:
                message = "背景已更新";
                break;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleMediaPicked(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null) {
            Toast.makeText(requireContext(), "无法识别文件类型", Toast.LENGTH_SHORT).show();
            return;
        }

        String mediaType;
        if (type.startsWith("video/")) {
            mediaType = "video";
        } else if (type.startsWith("image/")) {
            mediaType = "image";
            // Launch crop activity instead of directly saving
            launchCropActivity(uri, ASPECT_PROFILE, KEY_BG_URI);
            return;
        } else {
            Toast.makeText(requireContext(), "请选择视频或图片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save preference
        profilePrefs.edit()
                .putString(KEY_BG_URI, uri.toString())
                .putString(KEY_BG_TYPE, mediaType)
                .apply();

        applyBackgroundMedia(uri, mediaType);
        Toast.makeText(requireContext(), "背景已更新", Toast.LENGTH_SHORT).show();
    }

    private void applyBackgroundMedia(Uri uri, String type) {
        if ("video".equals(type)) {
            ivProfileBg.setVisibility(View.GONE);
            profileVideo.setVisibility(View.VISIBLE);
            profileVideo.setVideoURI(uri);
            profileVideo.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.setVolume(0f, 0f);
            });
            profileVideo.start();
            tvBgStatus.setText(R.string.profile_bg_custom_video);
        } else if ("image".equals(type)) {
            profileVideo.setVisibility(View.GONE);
            profileVideo.stopPlayback();
            ivProfileBg.setVisibility(View.VISIBLE);
            ivProfileBg.setImageURI(uri);
            tvBgStatus.setText(R.string.profile_bg_custom_image);
        }
    }

    /**
     * Resolve a stored path (URI string or file path) to a Uri.
     */
    private Uri resolveStoredPath(String storedPath) {
        if (storedPath == null) return null;
        if (storedPath.startsWith("content://")) {
            return Uri.parse(storedPath);
        }
        return Uri.fromFile(new java.io.File(storedPath));
    }

    private void resetBackgroundToDefault() {
        profilePrefs.edit()
                .remove(KEY_BG_URI)
                .remove(KEY_BG_TYPE)
                .apply();
        ivProfileBg.setVisibility(View.GONE);
        ivProfileBg.setImageURI(null);
        playDefaultVideo();
        Toast.makeText(requireContext(), "已恢复默认背景", Toast.LENGTH_SHORT).show();
    }

    // ---- Calendar Background ----


    private void showCalBgSettingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_calendar_bg, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        // Preview
        ImageView ivPreview = dialogView.findViewById(R.id.iv_cal_bg_preview);
        View placeholder = dialogView.findViewById(R.id.ll_cal_bg_placeholder);

        String savedUri = profilePrefs.getString(KEY_CAL_BG_URI, null);
        if (savedUri != null) {
            placeholder.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            ivPreview.setImageURI(resolveStoredPath(savedUri));
        } else {
            placeholder.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
        }

        dialogView.findViewById(R.id.btn_cal_bg_choose).setOnClickListener(v -> {
            dialog.dismiss();
            pickCalBgLauncher.launch(new String[]{"image/*"});
        });

        dialogView.findViewById(R.id.btn_cal_bg_reset).setOnClickListener(v -> {
            dialog.dismiss();
            resetCalBgToDefault();
        });

        dialogView.findViewById(R.id.btn_cal_bg_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleCalBgPicked(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith("image/")) {
            Toast.makeText(requireContext(), "请选择图片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        launchCropActivity(uri, ASPECT_CALENDAR, KEY_CAL_BG_URI);
    }

    private void resetCalBgToDefault() {
        profilePrefs.edit()
                .remove(KEY_CAL_BG_URI)
                .apply();
        Toast.makeText(requireContext(), "已恢复默认日历背景", Toast.LENGTH_SHORT).show();
    }

    // ---- Login Background ----

    private void showLoginBgSettingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_login_bg, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        ImageView ivPreview = dialogView.findViewById(R.id.iv_login_bg_preview);
        View placeholder = dialogView.findViewById(R.id.ll_login_bg_placeholder);

        String savedUri = profilePrefs.getString(KEY_LOGIN_BG_URI, null);
        if (savedUri != null) {
            placeholder.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            ivPreview.setImageURI(resolveStoredPath(savedUri));
        } else {
            placeholder.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
        }

        dialogView.findViewById(R.id.btn_login_bg_choose).setOnClickListener(v -> {
            dialog.dismiss();
            pickLoginBgLauncher.launch(new String[]{"image/*"});
        });

        dialogView.findViewById(R.id.btn_login_bg_reset).setOnClickListener(v -> {
            dialog.dismiss();
            resetLoginBgToDefault();
        });

        dialogView.findViewById(R.id.btn_login_bg_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleLoginBgPicked(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith("image/")) {
            Toast.makeText(requireContext(), "请选择图片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        launchCropActivity(uri, ASPECT_LOGIN, KEY_LOGIN_BG_URI);
    }

    private void resetLoginBgToDefault() {
        profilePrefs.edit()
                .remove(KEY_LOGIN_BG_URI)
                .apply();
        Toast.makeText(requireContext(), "已恢复默认登录页背景", Toast.LENGTH_SHORT).show();
    }

    // ---- Home Background ----

    private void showHomeBgSettingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_home_bg, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        ImageView ivPreview = dialogView.findViewById(R.id.iv_home_bg_preview);
        View placeholder = dialogView.findViewById(R.id.ll_home_bg_placeholder);

        String savedUri = profilePrefs.getString(KEY_HOME_BG_URI, null);
        if (savedUri != null) {
            placeholder.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            ivPreview.setImageURI(resolveStoredPath(savedUri));
        } else {
            placeholder.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
        }

        dialogView.findViewById(R.id.btn_home_bg_choose).setOnClickListener(v -> {
            dialog.dismiss();
            pickHomeBgLauncher.launch(new String[]{"image/*"});
        });

        dialogView.findViewById(R.id.btn_home_bg_reset).setOnClickListener(v -> {
            dialog.dismiss();
            resetHomeBgToDefault();
        });

        dialogView.findViewById(R.id.btn_home_bg_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleHomeBgPicked(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith("image/")) {
            Toast.makeText(requireContext(), "请选择图片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        launchCropActivity(uri, ASPECT_HOME_FULL, KEY_HOME_BG_URI);
    }

    private void resetHomeBgToDefault() {
        profilePrefs.edit()
                .remove(KEY_HOME_BG_URI)
                .apply();
        Toast.makeText(requireContext(), "已恢复默认主页背景", Toast.LENGTH_SHORT).show();
    }

    // ---- Dialog Bill Background ----

    private void showDialogBillBgSettingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dialog_bill_bg, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        ImageView ivPreview = dialogView.findViewById(R.id.iv_dialog_bill_bg_preview);
        View placeholder = dialogView.findViewById(R.id.ll_dialog_bill_bg_placeholder);

        String savedUri = profilePrefs.getString(KEY_DIALOG_BILL_BG_URI, null);
        if (savedUri != null) {
            placeholder.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            ivPreview.setImageURI(resolveStoredPath(savedUri));
        } else {
            placeholder.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
        }

        dialogView.findViewById(R.id.btn_dialog_bill_bg_choose).setOnClickListener(v -> {
            dialog.dismiss();
            pickDialogBillBgLauncher.launch(new String[]{"image/*"});
        });

        dialogView.findViewById(R.id.btn_dialog_bill_bg_reset).setOnClickListener(v -> {
            dialog.dismiss();
            resetDialogBillBgToDefault();
        });

        dialogView.findViewById(R.id.btn_dialog_bill_bg_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleDialogBillBgPicked(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith("image/")) {
            Toast.makeText(requireContext(), "请选择图片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        launchCropActivity(uri, ASPECT_DIALOG_BILL, KEY_DIALOG_BILL_BG_URI);
    }

    private void resetDialogBillBgToDefault() {
        profilePrefs.edit()
                .remove(KEY_DIALOG_BILL_BG_URI)
                .apply();
        Toast.makeText(requireContext(), "已恢复默认记账弹窗背景", Toast.LENGTH_SHORT).show();
    }

    // ---- Home Header Background (主页顶部背景) ----

    private void showHomeHeaderBgSettingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_home_header_bg, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        ImageView ivPreview = dialogView.findViewById(R.id.iv_home_header_bg_preview);
        View placeholder = dialogView.findViewById(R.id.ll_home_header_bg_placeholder);
        TextView tvCrop = dialogView.findViewById(R.id.tv_scale_crop);
        TextView tvFit = dialogView.findViewById(R.id.tv_scale_fit);

        // 当前选中的缩放模式（默认 centerCrop）
        final ImageView.ScaleType[] currentScaleType = {ImageView.ScaleType.CENTER_CROP};
        // 当前选中的图片 URI
        final Uri[] currentUri = {null};

        String savedUri = profilePrefs.getString(KEY_HOME_HEADER_BG_URI, null);
        if (savedUri != null) {
            placeholder.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            Uri uri = resolveStoredPath(savedUri);
            currentUri[0] = uri;
            ImageUtils.loadPreviewImage(requireContext(), ivPreview, uri, ImageView.ScaleType.CENTER_CROP);
        } else {
            placeholder.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
        }

        // 缩放模式切换：裁剪填充
        tvCrop.setOnClickListener(v -> {
            currentScaleType[0] = ImageView.ScaleType.CENTER_CROP;
            tvCrop.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary)));
            tvCrop.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white));
            tvFit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8ECF0));
            tvFit.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.ink));
            ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        });

        // 缩放模式切换：等比缩放
        tvFit.setOnClickListener(v -> {
            currentScaleType[0] = ImageView.ScaleType.FIT_CENTER;
            tvFit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary)));
            tvFit.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white));
            tvCrop.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8ECF0));
            tvCrop.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.ink));
            ivPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        });

        dialogView.findViewById(R.id.btn_home_header_bg_choose).setOnClickListener(v -> {
            dialog.dismiss();
            pickHomeHeaderBgLauncher.launch(new String[]{"image/*"});
        });

        dialogView.findViewById(R.id.btn_home_header_bg_reset).setOnClickListener(v -> {
            dialog.dismiss();
            resetHomeHeaderBgToDefault();
        });

        dialogView.findViewById(R.id.btn_home_header_bg_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleHomeHeaderBgPicked(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith("image/")) {
            Toast.makeText(requireContext(), "请选择图片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        launchCropActivity(uri, ASPECT_HOME_HEADER, KEY_HOME_HEADER_BG_URI);
    }

    private void resetHomeHeaderBgToDefault() {
        profilePrefs.edit()
                .remove(KEY_HOME_HEADER_BG_URI)
                .apply();
        Toast.makeText(requireContext(), "已恢复默认主页顶部背景", Toast.LENGTH_SHORT).show();
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
        if (profileVideo != null && profileVideo.getVisibility() == View.VISIBLE && !profileVideo.isPlaying()) {
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
                // 同时清除图片缓存
                ImageUtils.clearDiskCache(requireContext());
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "已清除 " + deletedCount + " 条旧记录", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "清除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateTransactionStyleDisplay() {
        String style = profilePrefs.getString(KEY_TRANSACTION_STYLE, STYLE_STANDARD);
        int resId;
        switch (style) {
            case STYLE_ISLAND:
                resId = R.string.style_island;
                break;
            default:
                resId = R.string.style_standard;
                break;
        }
        if (tvTransactionStyleValue != null) {
            tvTransactionStyleValue.setText(resId);
        }
    }

    private void showTransactionStyleDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_transaction_style, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        RadioButton rbStandard = dialogView.findViewById(R.id.rb_style_standard);
        RadioButton rbIsland = dialogView.findViewById(R.id.rb_style_island);
        View cardStandard = dialogView.findViewById(R.id.opt_style_standard);
        View cardIsland = dialogView.findViewById(R.id.opt_style_island);

        RadioButton[] radioButtons = {rbStandard, rbIsland};
        View[] cards = {cardStandard, cardIsland};

        // Helper to clear all radio buttons and reset card borders
        Runnable clearSelection = () -> {
            for (RadioButton rb : radioButtons) rb.setChecked(false);
            for (View card : cards) {
                card.setBackgroundResource(R.drawable.bg_style_card);
            }
        };

        // Helper to select a specific option with visual feedback
        class StyleSelector {
            void select(RadioButton rb, View card) {
                clearSelection.run();
                rb.setChecked(true);
                card.setBackgroundResource(R.drawable.bg_style_card_selected);
            }
        }
        StyleSelector selector = new StyleSelector();

        // Set current selection
        String currentStyle = profilePrefs.getString(KEY_TRANSACTION_STYLE, STYLE_STANDARD);
        switch (currentStyle) {
            case STYLE_ISLAND:
                selector.select(rbIsland, cardIsland);
                break;
            default:
                selector.select(rbStandard, cardStandard);
                break;
        }

        // Clicking anywhere on the card row selects that option
        cardStandard.setOnClickListener(v -> {
            selector.select(rbStandard, cardStandard);
            animateCardPress(cardStandard);
        });
        cardIsland.setOnClickListener(v -> {
            selector.select(rbIsland, cardIsland);
            animateCardPress(cardIsland);
        });

        // Save button
        dialogView.findViewById(R.id.btn_style_save).setOnClickListener(v -> {
            String selectedStyle;
            if (rbIsland.isChecked()) {
                selectedStyle = STYLE_ISLAND;
            } else {
                selectedStyle = STYLE_STANDARD;
            }
            profilePrefs.edit().putString(KEY_TRANSACTION_STYLE, selectedStyle).apply();
            updateTransactionStyleDisplay();
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.style_saved, Toast.LENGTH_SHORT).show();
        });

        // Cancel button
        dialogView.findViewById(R.id.btn_style_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Set dialog width to 85% of screen width and center on screen
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = android.view.Gravity.CENTER;
            window.setAttributes(lp);
        }
    }

    /** Applies a subtle press animation to the card when clicked */
    private void animateCardPress(View card) {
        card.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(80)
                .withEndAction(() -> card.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }

    private void showColorSchemeDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_color_scheme, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setView(dialogView)
                .create();

        RadioButton rbTeal = dialogView.findViewById(R.id.rb_scheme_teal);
        RadioButton rbLavender = dialogView.findViewById(R.id.rb_scheme_lavender);
        RadioButton rbOcean = dialogView.findViewById(R.id.rb_scheme_ocean);
        View optTeal = dialogView.findViewById(R.id.opt_scheme_teal);
        View optLavender = dialogView.findViewById(R.id.opt_scheme_lavender);
        View optOcean = dialogView.findViewById(R.id.opt_scheme_ocean);

        RadioButton[] radioButtons = {rbTeal, rbLavender, rbOcean};
        View[] cards = {optTeal, optLavender, optOcean};

        Runnable clearSelection = () -> {
            for (RadioButton rb : radioButtons) rb.setChecked(false);
            for (View card : cards) {
                card.setBackgroundResource(R.drawable.bg_style_card);
            }
        };

        class SchemeSelector {
            void select(RadioButton rb, View card) {
                clearSelection.run();
                rb.setChecked(true);
                card.setBackgroundResource(R.drawable.bg_style_card_selected);
            }
        }
        SchemeSelector selector = new SchemeSelector();

        // Set current selection
        String currentScheme = ColorSchemeManager.getSelectedScheme(requireContext());
        switch (currentScheme) {
            case ColorSchemeManager.SCHEME_LAVENDER_DREAM:
                selector.select(rbLavender, optLavender);
                break;
            case ColorSchemeManager.SCHEME_OCEAN_MINT:
                selector.select(rbOcean, optOcean);
                break;
            default:
                selector.select(rbTeal, optTeal);
                break;
        }

        optTeal.setOnClickListener(v -> {
            selector.select(rbTeal, optTeal);
            animateCardPress(optTeal);
        });
        optLavender.setOnClickListener(v -> {
            selector.select(rbLavender, optLavender);
            animateCardPress(optLavender);
        });
        optOcean.setOnClickListener(v -> {
            selector.select(rbOcean, optOcean);
            animateCardPress(optOcean);
        });

        dialogView.findViewById(R.id.btn_scheme_apply).setOnClickListener(v -> {
            String selectedScheme;
            if (rbLavender.isChecked()) {
                selectedScheme = ColorSchemeManager.SCHEME_LAVENDER_DREAM;
            } else if (rbOcean.isChecked()) {
                selectedScheme = ColorSchemeManager.SCHEME_OCEAN_MINT;
            } else {
                selectedScheme = ColorSchemeManager.SCHEME_TEAL_SAKURA;
            }
            ColorSchemeManager.setSelectedScheme(requireContext(), selectedScheme);
            dialog.dismiss();
            requireActivity().recreate();
        });

        dialogView.findViewById(R.id.btn_scheme_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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