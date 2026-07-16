package com.example.myapplication.ui.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.core.view.MotionEventCompat;

/**
 * Custom ImageView for image cropping.
 * Displays an image with a crop frame overlay that maintains a fixed aspect ratio.
 * Supports drag to move the frame and pinch to scale the frame.
 */
public class CropImageView extends ImageView {

    private static final int OVERLAY_COLOR = 0xAA000000;
    private static final int FRAME_COLOR = 0xFFFFFFFF;
    private static final int GRID_COLOR = 0x80FFFFFF;
    private static final float FRAME_STROKE_WIDTH = 2f;
    private static final float GRID_STROKE_WIDTH = 1f;
    private static final float CORNER_SIZE = 20f;
    private static final float CORNER_STROKE_WIDTH = 3f;
    private static final float MIN_FRAME_SIZE = 80f;

    private final Paint overlayPaint;
    private final Paint framePaint;
    private final Paint gridPaint;
    private final Paint cornerPaint;

    private RectF cropFrame;
    private float targetAspectRatio = 1.0f;
    private boolean isFrameInitialized = false;

    private float lastX;
    private float lastY;
    private int activePointerId = -1;
    private boolean isDragging = false;

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;

    private float imageDisplayWidth;
    private float imageDisplayHeight;
    private float imageOffsetX;
    private float imageOffsetY;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overlayPaint.setColor(OVERLAY_COLOR);
        overlayPaint.setStyle(Paint.Style.FILL);

        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(FRAME_COLOR);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(FRAME_STROKE_WIDTH);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(GRID_STROKE_WIDTH);

        cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setColor(FRAME_COLOR);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(CORNER_STROKE_WIDTH);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        setScaleType(ScaleType.FIT_CENTER);
    }

    public void setTargetAspectRatio(float aspectRatio) {
        this.targetAspectRatio = aspectRatio;
        this.isFrameInitialized = false;
        invalidate();
    }

    public void setImageUri(Uri uri) {
        setImageURI(uri);
        this.isFrameInitialized = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getDrawable() == null) return;

        if (!isFrameInitialized) {
            initCropFrame();
            isFrameInitialized = true;
        }

        calculateImageDisplayBounds();

        drawOverlay(canvas);
        drawFrame(canvas);
        drawGrid(canvas);
        drawCorners(canvas);
    }

    private void calculateImageDisplayBounds() {
        if (getDrawable() == null) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float drawableWidth = getDrawable().getIntrinsicWidth();
        float drawableHeight = getDrawable().getIntrinsicHeight();

        if (drawableWidth <= 0 || drawableHeight <= 0) return;

        float scale = Math.min(viewWidth / drawableWidth, viewHeight / drawableHeight);
        imageDisplayWidth = drawableWidth * scale;
        imageDisplayHeight = drawableHeight * scale;
        imageOffsetX = (viewWidth - imageDisplayWidth) / 2f;
        imageOffsetY = (viewHeight - imageDisplayHeight) / 2f;
    }

    private void initCropFrame() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) return;

        float frameWidth, frameHeight;

        if (targetAspectRatio >= 1.0f) {
            frameWidth = Math.min(viewWidth * 0.8f, viewHeight * 0.8f * targetAspectRatio);
            frameHeight = frameWidth / targetAspectRatio;
        } else {
            frameHeight = Math.min(viewHeight * 0.8f, viewWidth * 0.8f / targetAspectRatio);
            frameWidth = frameHeight * targetAspectRatio;
        }

        if (frameWidth > viewWidth * 0.9f) {
            frameWidth = viewWidth * 0.9f;
            frameHeight = frameWidth / targetAspectRatio;
        }
        if (frameHeight > viewHeight * 0.9f) {
            frameHeight = viewHeight * 0.9f;
            frameWidth = frameHeight * targetAspectRatio;
        }

        float left = (viewWidth - frameWidth) / 2f;
        float top = (viewHeight - frameHeight) / 2f;
        cropFrame = new RectF(left, top, left + frameWidth, top + frameHeight);
    }

    private void drawOverlay(Canvas canvas) {
        float left = cropFrame.left;
        float top = cropFrame.top;
        float right = cropFrame.right;
        float bottom = cropFrame.bottom;
        float w = getWidth();
        float h = getHeight();

        canvas.drawRect(0, 0, w, top, overlayPaint);
        canvas.drawRect(0, bottom, w, h, overlayPaint);
        canvas.drawRect(0, top, left, bottom, overlayPaint);
        canvas.drawRect(right, top, w, bottom, overlayPaint);
    }

    private void drawFrame(Canvas canvas) {
        canvas.drawRect(cropFrame, framePaint);
    }

    private void drawGrid(Canvas canvas) {
        float left = cropFrame.left;
        float top = cropFrame.top;
        float right = cropFrame.right;
        float bottom = cropFrame.bottom;
        float thirdW = (right - left) / 3f;
        float thirdH = (bottom - top) / 3f;

        canvas.drawLine(left + thirdW, top, left + thirdW, bottom, gridPaint);
        canvas.drawLine(left + 2 * thirdW, top, left + 2 * thirdW, bottom, gridPaint);
        canvas.drawLine(left, top + thirdH, right, top + thirdH, gridPaint);
        canvas.drawLine(left, top + 2 * thirdH, right, top + 2 * thirdH, gridPaint);
    }

    private void drawCorners(Canvas canvas) {
        float left = cropFrame.left;
        float top = cropFrame.top;
        float right = cropFrame.right;
        float bottom = cropFrame.bottom;

        canvas.drawLine(left, top, left + CORNER_SIZE, top, cornerPaint);
        canvas.drawLine(left, top, left, top + CORNER_SIZE, cornerPaint);

        canvas.drawLine(right, top, right - CORNER_SIZE, top, cornerPaint);
        canvas.drawLine(right, top, right, top + CORNER_SIZE, cornerPaint);

        canvas.drawLine(left, bottom, left + CORNER_SIZE, bottom, cornerPaint);
        canvas.drawLine(left, bottom, left, bottom - CORNER_SIZE, cornerPaint);

        canvas.drawLine(right, bottom, right - CORNER_SIZE, bottom, cornerPaint);
        canvas.drawLine(right, bottom, right, bottom - CORNER_SIZE, cornerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cropFrame == null) return true;

        scaleDetector.onTouchEvent(event);

        int action = MotionEventCompat.getActionMasked(event);
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInsideFrame(x, y)) {
                    activePointerId = event.getPointerId(0);
                    lastX = x;
                    lastY = y;
                    isDragging = true;
                } else {
                    isDragging = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && activePointerId != -1 && !scaleDetector.isInProgress()) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex >= 0) {
                        float dx = event.getX(pointerIndex) - lastX;
                        float dy = event.getY(pointerIndex) - lastY;
                        moveFrame(dx, dy);
                        lastX = event.getX(pointerIndex);
                        lastY = event.getY(pointerIndex);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                activePointerId = -1;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                int pointerIndex = MotionEventCompat.getActionIndex(event);
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    activePointerId = event.getPointerId(newPointerIndex);
                    lastX = event.getX(newPointerIndex);
                    lastY = event.getY(newPointerIndex);
                }
                break;
        }
        return true;
    }

    private boolean isInsideFrame(float x, float y) {
        float padding = 30f;
        return x >= cropFrame.left - padding && x <= cropFrame.right + padding
                && y >= cropFrame.top - padding && y <= cropFrame.bottom + padding;
    }

    private void moveFrame(float dx, float dy) {
        float newLeft = cropFrame.left + dx;
        float newTop = cropFrame.top + dy;
        float newRight = cropFrame.right + dx;
        float newBottom = cropFrame.bottom + dy;

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if (newLeft < 0) {
            newRight -= newLeft;
            newLeft = 0;
        }
        if (newTop < 0) {
            newBottom -= newTop;
            newTop = 0;
        }
        if (newRight > viewWidth) {
            newLeft -= (newRight - viewWidth);
            newRight = viewWidth;
        }
        if (newBottom > viewHeight) {
            newTop -= (newBottom - viewHeight);
            newBottom = viewHeight;
        }

        float frameWidth = newRight - newLeft;
        float frameHeight = newBottom - newTop;

        if (frameWidth < MIN_FRAME_SIZE || frameHeight < MIN_FRAME_SIZE) {
            return;
        }

        cropFrame.set(newLeft, newTop, newRight, newBottom);
        invalidate();
    }

    private void scaleFrame(float scaleFactor) {
        float centerX = cropFrame.centerX();
        float centerY = cropFrame.centerY();

        float newWidth = cropFrame.width() * scaleFactor;
        float newHeight = newWidth / targetAspectRatio;

        float minDim = Math.min(getWidth(), getHeight()) * 0.15f;
        if (newWidth < minDim || newHeight < minDim) {
            newWidth = minDim;
            newHeight = newWidth / targetAspectRatio;
        }

        float maxDim = Math.min(getWidth(), getHeight()) * 0.95f;
        if (newWidth > maxDim || newHeight > maxDim) {
            if (newWidth > newHeight) {
                newWidth = maxDim;
                newHeight = newWidth / targetAspectRatio;
            } else {
                newHeight = maxDim;
                newWidth = newHeight * targetAspectRatio;
            }
        }

        float newLeft = centerX - newWidth / 2f;
        float newTop = centerY - newHeight / 2f;
        float newRight = newLeft + newWidth;
        float newBottom = newTop + newHeight;

        if (newLeft < 0) {
            newRight -= newLeft;
            newLeft = 0;
        }
        if (newTop < 0) {
            newBottom -= newTop;
            newTop = 0;
        }
        if (newRight > getWidth()) {
            newLeft -= (newRight - getWidth());
            newRight = getWidth();
        }
        if (newBottom > getHeight()) {
            newTop -= (newBottom - getHeight());
            newBottom = getHeight();
        }

        cropFrame.set(newLeft, newTop, newRight, newBottom);
        invalidate();
    }

    /**
     * Get the cropped bitmap based on the current crop frame.
     */
    public Bitmap getCroppedBitmap() {
        if (getDrawable() == null || cropFrame == null) return null;

        Bitmap sourceBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        if (sourceBitmap == null) return null;

        calculateImageDisplayBounds();

        float scaleX = sourceBitmap.getWidth() / imageDisplayWidth;
        float scaleY = sourceBitmap.getHeight() / imageDisplayHeight;

        float cropLeft = (cropFrame.left - imageOffsetX) * scaleX;
        float cropTop = (cropFrame.top - imageOffsetY) * scaleY;
        float cropWidth = cropFrame.width() * scaleX;
        float cropHeight = cropFrame.height() * scaleY;

        cropLeft = Math.max(0, cropLeft);
        cropTop = Math.max(0, cropTop);
        cropWidth = Math.min(cropWidth, sourceBitmap.getWidth() - cropLeft);
        cropHeight = Math.min(cropHeight, sourceBitmap.getHeight() - cropTop);

        if (cropWidth <= 0 || cropHeight <= 0) return null;

        try {
            return Bitmap.createBitmap(sourceBitmap, (int) cropLeft, (int) cropTop,
                    (int) cropWidth, (int) cropHeight);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reset crop frame to default centered position.
     */
    public void resetCropFrame() {
        isFrameInitialized = false;
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor = detector.getScaleFactor();
            scaleFrame(scaleFactor);
            return true;
        }
    }
}