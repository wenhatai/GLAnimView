package com.parr.glview;

import java.util.LinkedList;

import android.content.Context;

public class FrameSprite extends Sprite {
    public int fps = 10;
    
    private Texture[] mTextures;
    private String[] mFileNames;
    private boolean mIsStart;
    private long mStartTime;
    private int mCurrentFrameIndex = -1;
    private LinkedList<Texture> mTextureList = new LinkedList<Texture>();
    private boolean mIsStayAtLastFrame = false;
    private boolean mIsReleased = false;
    public OnFrameEndListener mFrameEndListener;
    
    public FrameSprite(String[] fileNames) {
        mFileNames = fileNames;
        mTextures = new Texture[fileNames.length];
    }

    public void setStayAtLastFrame(boolean isStayAtLastFrame) {
        mIsStayAtLastFrame = isStayAtLastFrame;
    }
    
    public void decode(final Context context) {
        new Thread() {
            public void run() {
                for (int i =0; i < mFileNames.length && !mIsReleased; i++) {
                    Texture texture = new Texture(context, mFileNames[i]);
                    System.out.println("FrameSprite: deode:" +  i);
                    putTexture(i, texture);
                    if (i == 9) {
                        startDraw();
                    }
                    while (mTextureList != null && mTextureList.size() >= 10) {
                        // 超过9个，等待
                        try {
                            sleep(16);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }.start();
    }
    
    private void putTexture(int index, Texture texture) {
        synchronized (this) {
            if (mTextureList != null) {
                mTextureList.add(texture);
                mTextures[index] = texture;
            }
        }
    }
    
    private Texture getTexture(int index) {
        synchronized (this) {
            if (mTextureList != null) {
                mTextureList.remove(mTextures[index]);
                return mTextures[index];
            } else {
                return null;
            }
        }
    }
    
    private void clearTexture() {
        synchronized (this) {
            for (int i = 0; i < mTextureList.size(); i++) {
                mTextureList.get(i).release();
            }
            mTextureList.clear();
        }
    }

    public void startDraw() {
        mIsStart = true;
        mStartTime = System.currentTimeMillis();
    }
    
    @Override
    public void draw(int viewWidth, int viewHeight, int attribPosition, int attribTexCoord, int mvpMatrixHandle, int textureHandle, int alphaHanle, float[] mvpMatrix) {
        if (mIsStart) {
            long time = System.currentTimeMillis() - mStartTime;
            int index = (int) (time / (1000.0f / fps));
            if (index != mCurrentFrameIndex) {
                mCurrentFrameIndex = index;
                if (mCurrentFrameIndex < mTextures.length) {
                    if (mTextures[mCurrentFrameIndex] != null) {
                        if (mTexture != null) {
                            mTexture.release();
                        }
                        mTexture = getTexture(mCurrentFrameIndex);
                        init();
                        super.draw(viewWidth, viewHeight, attribPosition, attribTexCoord, mvpMatrixHandle, textureHandle, alphaHanle, mvpMatrix);
                        System.out.println("FrameSprite: draw1:" +  mCurrentFrameIndex);
                    } else {
                        System.out.println("FrameSprite: mTexture = null:" +  mCurrentFrameIndex);
                        clearTexture();
                    }
                } else {
                    if (mIsStayAtLastFrame) {
                        super.draw(viewWidth, viewHeight, attribPosition, attribTexCoord, mvpMatrixHandle, textureHandle, alphaHanle, mvpMatrix);
                        System.out.println("FrameSprite: draw2:" +  (mTextures.length - 1));
                    } else {
                        mIsStart = false;
                        if (mTexture != null) {
                            mTexture.release();
                        }
                    }
                    if (mFrameEndListener != null) {
                        mFrameEndListener.onFrameEnd();
                        mFrameEndListener = null;
                    }
                }
            } else {
                super.draw(viewWidth, viewHeight, attribPosition, attribTexCoord, mvpMatrixHandle, textureHandle, alphaHanle, mvpMatrix);
                System.out.println("FrameSprite: draw3:" +  mCurrentFrameIndex);
            }
        }
    }
    
    public static interface OnFrameEndListener{
        /**
         *
         */
        public void onFrameEnd();
    }
    
    @Override
    public void release() {
        for (int i = 0; i < mTextures.length; i++) {
            if (mTextures[i] != null) {
                mTextures[i].release();
                mTextures[i] = null;
            }
        }
        mTextureList.clear();
        mTextureList = null;
        mTextures = null;
        mIsReleased = true;
        super.release();
    }
}
