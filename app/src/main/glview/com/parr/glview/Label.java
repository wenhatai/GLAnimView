package com.parr.glview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Label extends Sprite {
    private String mText = "";
    private int mTextSize = 20;
    private int mTextColor = Color.WHITE;
    private Paint mPaint;

    public Label(SpriteGLView spriteGLView, Context context, String text, int color, int textSizeWithPx) {
        mSpriteGLView = spriteGLView;
        mPaint = new Paint();
        setTextColor(color);
        setTextSize(textSizeWithPx);
        setText(text);
    }

    public void setText(String text) {
        if (text.equals(mText)) {
            return;
        }
        mText = text;
        int textLength = (int) mPaint.measureText(text);
        Bitmap bitmap = Bitmap.createBitmap(textLength, mTextSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK, android.graphics.PorterDuff.Mode.CLEAR);
        canvas.drawText(mText, 0, mTextSize * 0.8f, mPaint);
        if (mTexture != null) {
            mTexture.release();
        }
        mTexture = new Texture(bitmap);
        init();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        mPaint.setColor(mTextColor);
    }

    public void setTextSize(int px) {
        mTextSize = px;
        mPaint.setTextSize(mTextSize);
    }
}
