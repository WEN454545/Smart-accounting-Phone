package com.example.autobookkeep.ui;

import android.content.Context;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.myapplication.R;

public class CustomHighlightEditText extends AppCompatEditText {

    private int highlightBackgroundColor;
    private int highlightTextColor;
    private boolean hasFocus = false;

    public CustomHighlightEditText(Context context) {
        super(context);
        init(context);
    }

    public CustomHighlightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomHighlightEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        highlightBackgroundColor = context.getResources().getColor(R.color.app_blue, null);
        highlightTextColor = context.getResources().getColor(android.R.color.white, null);

        setTextIsSelectable(true);
        setLongClickable(true);
    }

    public void setHighlightColors(int backgroundColor, int textColor) {
        this.highlightBackgroundColor = backgroundColor;
        this.highlightTextColor = textColor;
        invalidate();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, android.graphics.Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        hasFocus = focused;
        invalidate();
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        if (hasFocus) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();

            if (selStart >= 0 && selEnd > selStart && getText() != null) {
                Spannable text = (Spannable) getText();
                clearHighlightSpans(text);
                text.setSpan(new BackgroundColorSpan(highlightBackgroundColor),
                        selStart, selEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new ForegroundColorSpan(highlightTextColor),
                        selStart, selEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                clearHighlightSpans((Spannable) getText());
            }
        }
        super.onDraw(canvas);
    }

    private void clearHighlightSpans(Spannable text) {
        if (text == null) return;
        BackgroundColorSpan[] bgSpans = text.getSpans(0, text.length(), BackgroundColorSpan.class);
        ForegroundColorSpan[] fgSpans = text.getSpans(0, text.length(), ForegroundColorSpan.class);
        for (BackgroundColorSpan span : bgSpans) {
            text.removeSpan(span);
        }
        for (ForegroundColorSpan span : fgSpans) {
            text.removeSpan(span);
        }
    }
}