package com.example.myapplication.ui.crop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Image cropping activity.
 * Receives a source image URI and target aspect ratio, displays a crop interface,
 * and returns the cropped image file path.
 */
public class CropImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_ASPECT_RATIO = "aspect_ratio";
    public static final String EXTRA_PREF_KEY = "pref_key";
    public static final String EXTRA_RESULT_PATH = "result_path";

    private CropImageView cropImageView;
    private Uri sourceUri;
    private float aspectRatio;
    private String prefKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.example.myapplication.MyApplication.getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        cropImageView = findViewById(R.id.crop_image_view);

        Intent intent = getIntent();
        sourceUri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        aspectRatio = intent.getFloatExtra(EXTRA_ASPECT_RATIO, 1.0f);
        prefKey = intent.getStringExtra(EXTRA_PREF_KEY);

        if (sourceUri == null) {
            Toast.makeText(this, R.string.crop_no_image, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cropImageView.setTargetAspectRatio(aspectRatio);
        cropImageView.setImageUri(sourceUri);

        findViewById(R.id.btn_crop_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.btn_crop_reset).setOnClickListener(v -> cropImageView.resetCropFrame());

        findViewById(R.id.btn_crop_confirm).setOnClickListener(v -> {
            Bitmap cropped = cropImageView.getCroppedBitmap();
            if (cropped == null) {
                Toast.makeText(this, R.string.crop_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            String savedPath = saveCroppedBitmap(cropped);
            cropped.recycle();

            if (savedPath != null) {
                Intent result = new Intent();
                result.putExtra(EXTRA_RESULT_PATH, savedPath);
                result.putExtra(EXTRA_PREF_KEY, prefKey);
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this, R.string.crop_save_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String saveCroppedBitmap(Bitmap bitmap) {
        try {
            File dir = new File(getCacheDir(), "crop_cache");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "crop_" + System.currentTimeMillis() + ".jpg";
            File file = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}