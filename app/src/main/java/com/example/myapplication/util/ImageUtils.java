package com.example.myapplication.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Image processing utility class.
 * Provides sampled decoding, proportional compression, disk caching, and one-stop async loading.
 * Solves OOM issues with large images, stretching, and deformation problems.
 */
public class ImageUtils {

    private static final String CACHE_DIR = "bg_cache";
    private static final int MAX_DECODE_WIDTH = 2048;
    private static final int MAX_DECODE_HEIGHT = 2048;
    private static final int COMPRESS_QUALITY = 85;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Decode a sampled Bitmap from URI (two-phase decoding).
     * Phase 1: Read dimensions only (inJustDecodeBounds=true).
     * Phase 2: Calculate inSampleSize and actually decode.
     *
     * @param context   Context
     * @param uri       Image URI
     * @param reqWidth  Target width in px
     * @param reqHeight Target height in px
     * @return Sampled Bitmap, or null on failure
     */
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculate inSampleSize (power of 2).
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Proportionally scale and compress Bitmap.
     *
     * @param source    Source Bitmap
     * @param maxWidth  Max width
     * @param maxHeight Max height
     * @param quality   JPEG compress quality (1-100)
     * @return Compressed Bitmap
     */
    public static Bitmap compressBitmap(Bitmap source, int maxWidth, int maxHeight, int quality) {
        if (source == null) return null;

        int width = source.getWidth();
        int height = source.getHeight();

        float scale = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        if (scale >= 1.0f) {
            return source;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap scaled = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        if (scaled != source) {
            source.recycle();
        }
        return scaled;
    }

    /**
     * Generate MD5 cache key from URI.
     */
    public static String getCacheKey(Uri uri) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(uri.toString().getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(uri.hashCode());
        }
    }

    /**
     * Get cache directory.
     */
    private static File getCacheDir(Context context) {
        File dir = new File(context.getCacheDir(), CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Save Bitmap to disk cache asynchronously.
     */
    public static void saveToDiskCache(Context context, Bitmap bitmap, String cacheKey) {
        if (bitmap == null || cacheKey == null) return;

        executor.execute(() -> {
            try {
                File cacheFile = new File(getCacheDir(context), cacheKey + ".jpg");
                FileOutputStream fos = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fos);
                fos.flush();
                fos.close();
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Load Bitmap from disk cache.
     */
    public static Bitmap loadFromDiskCache(Context context, String cacheKey) {
        if (cacheKey == null) return null;

        try {
            File cacheFile = new File(getCacheDir(context), cacheKey + ".jpg");
            if (cacheFile.exists()) {
                return BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Clear all disk cache.
     */
    public static void clearDiskCache(Context context) {
        executor.execute(() -> {
            try {
                File dir = getCacheDir(context);
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * One-stop background image loading.
     * Checks disk cache first; on miss, decodes, compresses, caches, and sets on main thread.
     *
     * @param context   Context
     * @param imageView Target ImageView
     * @param uri       Image URI
     * @param cacheKey  Cache key
     * @param scaleType Scale type
     */
    public static void loadBackgroundImage(Context context, ImageView imageView, Uri uri,
                                            String cacheKey, ImageView.ScaleType scaleType) {
        if (context == null || imageView == null || uri == null) return;

        Bitmap cached = loadFromDiskCache(context, cacheKey);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            imageView.setScaleType(scaleType);
            return;
        }

        executor.execute(() -> {
            try {
                Bitmap sampled = decodeSampledBitmapFromUri(context, uri, MAX_DECODE_WIDTH, MAX_DECODE_HEIGHT);
                if (sampled == null) {
                    mainHandler.post(() -> imageView.setImageURI(uri));
                    return;
                }

                Bitmap compressed = compressBitmap(sampled, MAX_DECODE_WIDTH, MAX_DECODE_HEIGHT, COMPRESS_QUALITY);

                saveBitmapToCacheSync(context, compressed, cacheKey);

                Bitmap finalBitmap = compressed;
                mainHandler.post(() -> {
                    imageView.setImageBitmap(finalBitmap);
                    imageView.setScaleType(scaleType);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    try {
                        imageView.setImageURI(uri);
                        imageView.setScaleType(scaleType);
                    } catch (Exception ignored) {
                    }
                });
            }
        });
    }

    /**
     * Synchronously save Bitmap to cache (called from background thread).
     */
    private static void saveBitmapToCacheSync(Context context, Bitmap bitmap, String cacheKey) {
        if (bitmap == null || cacheKey == null) return;
        try {
            File cacheFile = new File(getCacheDir(context), cacheKey + ".jpg");
            FileOutputStream fos = new FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fos);
            fos.flush();
            fos.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Load preview image (sampled decode only, no caching).
     * Used for preview scenarios.
     */
    public static void loadPreviewImage(Context context, ImageView imageView, Uri uri,
                                         ImageView.ScaleType scaleType) {
        if (context == null || imageView == null || uri == null) return;

        executor.execute(() -> {
            try {
                Bitmap sampled = decodeSampledBitmapFromUri(context, uri, MAX_DECODE_WIDTH, MAX_DECODE_HEIGHT);
                mainHandler.post(() -> {
                    if (sampled != null) {
                        imageView.setImageBitmap(sampled);
                    } else {
                        imageView.setImageURI(uri);
                    }
                    if (scaleType != null) {
                        imageView.setScaleType(scaleType);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    try {
                        imageView.setImageURI(uri);
                        if (scaleType != null) {
                            imageView.setScaleType(scaleType);
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        });
    }
}