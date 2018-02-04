package com.parr.glview;

import android.graphics.Bitmap;
import android.view.MotionEvent;

public class ImageButton extends Sprite implements Sprite.Clickable {
    private boolean mIsPressed = false;
    private OnClickListener mListener;
    private boolean mIsHalfOpacity = false;

    public ImageButton(SpriteGLView spriteGLView, Bitmap bitmap, boolean isHalfOpacity) {
        super(spriteGLView, bitmap);
        mIsHalfOpacity = isHalfOpacity;
    }

    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, int width, int height) {
        if (mTexture == null || mTexture.bitmap == null) {
            return false;
        }
        int action = event.getAction();
        float x = event.getX() ;
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            if (x > this.x - mTexture.bitmap.getWidth() * scale / 2 && x < this.x + mTexture.bitmap.getWidth() * scale / 2
                    && y > this.y - mTexture.bitmap.getHeight() * scale / 2 && y < this.y + mTexture.bitmap.getHeight() * scale / 2) {
                mIsPressed = true;
                if (mIsHalfOpacity) {
                    opacity = 128;
                }
            } else {
                mIsPressed = false;
                if (mIsHalfOpacity) {
                    opacity = 255;
                }
            }
            return mIsPressed;
        } else if (action == MotionEvent.ACTION_UP) {
            if (x > this.x - mTexture.bitmap.getWidth() * scale / 2 && x < this.x + mTexture.bitmap.getWidth() * scale / 2
                    && y > this.y - mTexture.bitmap.getHeight() * scale / 2 && y < this.y + mTexture.bitmap.getHeight() * scale / 2) {
                if (mListener != null) {
                    mListener.onClick(this);
                }
            }
            mIsPressed = false;
            if (mIsHalfOpacity) {
                opacity = 255;
            }
        }
        return false;
    }

    public interface OnClickListener {
        /**
         * 这里可以直接操作UI
         * @param sprite
         */
        public void onClick(Sprite sprite);
    }
}
