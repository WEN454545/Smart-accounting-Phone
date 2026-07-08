package com.example.myapplication.ui.auth;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DynamicBackgroundView extends View {

    private static final int PARTICLE_COUNT = 40;
    private static final long ANIMATION_DURATION_MS = 3000;

    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ValueAnimator animator;
    private float animationProgress = 0f;

    // Gradient colors that shift over time
    private final int[][] colorPairs = {
        {0xFF0D9488, 0xFF0F766E}, // Teal
        {0xFF1A1A2E, 0xFF16213E}, // Deep navy
        {0xFF0F3443, 0xFF0D9488}, // Dark teal
        {0xFF1B1B2F, 0xFF1A535C}, // Midnight
    };

    public DynamicBackgroundView(Context context) {
        super(context);
        init();
    }

    public DynamicBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        particlePaint.setAntiAlias(true);
        particlePaint.setStyle(Paint.Style.FILL);

        overlayPaint.setStyle(Paint.Style.FILL);
        overlayPaint.setColor(Color.TRANSPARENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initParticles();
        startAnimation();
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * getWidth();
            p.y = random.nextFloat() * getHeight();
            p.radius = 2f + random.nextFloat() * 8f;
            p.speedY = 0.3f + random.nextFloat() * 1.2f;
            p.speedX = (random.nextFloat() - 0.5f) * 0.5f;
            p.alpha = 0.1f + random.nextFloat() * 0.35f;
            p.wobblePhase = random.nextFloat() * (float) (2 * Math.PI);
            p.wobbleAmplitude = 10f + random.nextFloat() * 30f;
            p.color = generateParticleColor();
            particles.add(p);
        }
    }

    private int generateParticleColor() {
        float hue = random.nextFloat() * 360f;
        return ColorUtils.HSLToColor(new float[]{hue, 0.3f, 0.7f});
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationProgress = animation.getAnimatedFraction();
            updateParticles();
            invalidate();
        });
        animator.start();
    }

    private void updateParticles() {
        for (Particle p : particles) {
            // Move upward
            p.y -= p.speedY;

            // Wobble horizontally
            p.x += p.speedX + Math.sin(p.y * 0.02 + p.wobblePhase) * 0.3f;

            // Wrap around when off screen
            if (p.y + p.radius < 0) {
                p.y = getHeight() + p.radius;
                p.x = random.nextFloat() * getWidth();
                p.alpha = 0.1f + random.nextFloat() * 0.35f;
            }
            if (p.x < -p.radius) p.x = getWidth() + p.radius;
            if (p.x > getWidth() + p.radius) p.x = -p.radius;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawParticles(canvas);
    }

    private void drawBackground(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        // Interpolate between two color pairs for smooth transition
        int pairIndex = (int) (animationProgress * (colorPairs.length - 1));
        int nextPairIndex = Math.min(pairIndex + 1, colorPairs.length - 1);
        float t = (animationProgress * (colorPairs.length - 1)) - pairIndex;

        int[] currentColors = colorPairs[pairIndex];
        int[] nextColors = colorPairs[nextPairIndex];

        int topColor = ColorUtils.blendARGB(currentColors[0], nextColors[0], t);
        int bottomColor = ColorUtils.blendARGB(currentColors[1], nextColors[1], t);

        LinearGradient gradient = new LinearGradient(
            0, 0, 0, h,
            topColor, bottomColor,
            Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, w, h, bgPaint);

        // Draw a subtle radial glow
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float glowRadius = Math.max(w, h) * 0.6f;
        int glowColor = ColorUtils.setAlphaComponent(
            ColorUtils.blendARGB(0xFF0D9488, 0xFF22C55E, 0.3f),
            (int) (20 + 10 * Math.sin(animationProgress * Math.PI * 2))
        );
        glowPaint.setShader(new android.graphics.RadialGradient(
            w * 0.5f, h * 0.7f, glowRadius,
            glowColor, Color.TRANSPARENT,
            android.graphics.Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, w, h, glowPaint);
    }

    private void drawParticles(Canvas canvas) {
        for (Particle p : particles) {
            particlePaint.setAlpha((int) (p.alpha * 255));
            particlePaint.setColor(p.color);

            // Draw outer glow
            particlePaint.setShadowLayer(p.radius * 2, 0, 0,
                ColorUtils.setAlphaComponent(p.color, (int) (p.alpha * 80)));
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
            particlePaint.setShadowLayer(0, 0, 0, 0);

            // Draw inner bright core
            particlePaint.setAlpha((int) (p.alpha * 200));
            particlePaint.setColor(ColorUtils.blendARGB(p.color, Color.WHITE, 0.3f));
            canvas.drawCircle(p.x, p.y, p.radius * 0.5f, particlePaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    private static class Particle {
        float x, y;
        float radius;
        float speedY, speedX;
        float alpha;
        float wobblePhase;
        float wobbleAmplitude;
        int color;
    }
}