package com.example.myapplication.ui.auth;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * VideoView that stretches video to fill its bounds, ignoring aspect ratio.
 */
public class ScalableVideoView extends VideoView {

    public ScalableVideoView(Context context) {
        super(context);
    }

    public ScalableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Force the video to fill whatever size the layout dictates
        setMeasuredDimension(
                getDefaultSize(0, widthMeasureSpec),
                getDefaultSize(0, heightMeasureSpec));
    }
}