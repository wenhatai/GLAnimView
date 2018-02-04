package com.parr.glview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.parr.glview.Sprite.Clickable;
import com.parr.glview.util.GLUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class SpriteGLView extends GLSurfaceView {
    private List<Sprite> mSprites = Collections.synchronizedList(new ArrayList<Sprite>()); // 采用同步的list
    private List<Runnable> mUpdateCallbacks = new LinkedList<Runnable>();
    private Object mCallbackLock = new Object();
    public boolean isShowFps = false;
    public int isVideoHandle;

    public SpriteGLView(Context context) {
        super(context);
        init();
    }

    public SpriteGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 增加绘制对象
     * 
     * @param sprite
     *            绘制对象
     */
    public void addSprite(Sprite sprite) {
        for (int i = 0; i < mSprites.size(); i++) {
            if (mSprites.get(i) == sprite) {
                return;
            }
        }
        mSprites.add(sprite);
    }

    /**
     * 增加绘制对象
     * 
     * @param position
     *            层级
     * @param sprite
     *            绘制对象
     */
    public void addSprite(int position, Sprite sprite) {
        for (int i = 0; i < mSprites.size(); i++) {
            if (mSprites.get(i) == sprite) {
                return;
            }
        }
        mSprites.add(position, sprite);
    }

    /**
     * 移除绘制对象
     * 
     * @param sprite
     *            绘制对象
     */
    public void removeSprite(Sprite sprite) {
        if (sprite.isAutoRelease) {
            sprite.release();
        }
        for (int i = 0; i < mSprites.size(); i++) {
            if (mSprites.get(i) == sprite) {
                mSprites.remove(i);
                return;
            }
        }
    }

    public boolean isEmpty() {
        return mSprites.isEmpty();
    }

    public void runOnSurfaceViewThread(Runnable runnable) {
        synchronized (mCallbackLock) {
            mUpdateCallbacks.add(runnable);
        }
    }

    /**
     * 停止绘制
     */
    public void stopDraw() {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        for (int i = 0; i < mSprites.size(); i++) {
            Sprite sprite = mSprites.get(i);
            sprite.release();
        }
    }

    private void init() {
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(2);
        setRenderer(new SimpleRenderer());
        setFocusableInTouchMode(true);
    }

    private class SimpleRenderer implements Renderer {
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mCameraMatrix = new float[16];
        private final float[] mMVPMatrix = new float[16];

        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;
        private int mTexCoordHandle;
        private int mTexSamplerHandle;
        private int mAlphaHandle;
        
        private long mLastTime;
        private int frameCount = 0;
        private Label mLabel;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            mLabel = new Label(SpriteGLView.this, getContext(), "fps:", Color.WHITE, 40);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, GLUtil.readFromAssets(getContext(), "VertexShader.glsl"));
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, GLUtil.readFromAssets(getContext(), "FragmentShader.glsl"));
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
            mAlphaHandle = GLES20.glGetUniformLocation(mProgram, "v_alpha");
            isVideoHandle = GLES20.glGetUniformLocation(mProgram, "v_isVideo");
            
            float ratio = (float) width / height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
            Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);
            
            mLabel.x = 50 + mLabel.width - width / 2;
            mLabel.y = 50 - height / 2;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onDrawFrame(GL10 unused) {
            for (int i = 0; i < mUpdateCallbacks.size(); i++) {
                mUpdateCallbacks.get(i).run();
            }
            mUpdateCallbacks.clear();
            
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            for (int i = 0; i < mSprites.size(); i++) {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                GLES20.glUseProgram(mProgram);
                mSprites.get(i).draw(getWidth(), getHeight(), mPositionHandle, mTexCoordHandle, mMatrixHandle, mTexSamplerHandle, mAlphaHandle, mMVPMatrix);
            }
            // 绘制帧率
            if (isShowFps) {
                frameCount++;
                if (frameCount >= 5) {
                    int time = (int) (System.currentTimeMillis() - mLastTime);
                    String fps = String.format("fps: %.1f", frameCount * 1000.0f / time);
                    mLastTime = System.currentTimeMillis();
                    frameCount = 0;
                    mLabel.setText(fps);
                }
                GLES20.glUseProgram(mProgram);
                mLabel.draw(getWidth(), getHeight(), mPositionHandle, mTexCoordHandle, mMatrixHandle, mTexSamplerHandle, mAlphaHandle, mMVPMatrix);
            }
            GLES20.glFinish();
        }

        private int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (int i = 0; i < mSprites.size(); i++) {
            if (mSprites.get(i) instanceof Clickable) {
                boolean flag = ((Clickable) mSprites.get(i)).onTouchEvent(event, getWidth(), getHeight());
                if (flag) {
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}