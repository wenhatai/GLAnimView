package com.parr.glview;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.parr.glspriteviewdemo.R;
import com.parr.glview.FrameSprite.OnFrameEndListener;
import com.parr.glview.util.RawResourceReader;
import com.parr.glview.util.ShaderHelper;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Surface;

public class VideoSprite extends Sprite implements OnFrameAvailableListener {
    private boolean mFrameAvailable = false;
    private int mTextureParamHandle;
    private int mTextureCoordinateHandle;
    private int mTextureAlphaCoordinateHandle;
    private int mPositionHandle;
    private int mTextureTranformHandle;
    
    private float mSquareCoords[] = {
            -1,  1,   // top left
            -1, -1,   // bottom left
             1, -1,   // bottom right
             1,  1};  // top right

    private static final short sDrawOrder[] = {0, 1, 2, 0, 2, 3};
    private Context mContext;

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mTextureBuffer2;
    private static final float HALF = 0.5f;
    private float mTextureCoords[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            HALF, 0.0f, 0.0f, 1.0f,
            HALF, 1.0f, 0.0f, 1.0f};
    private float mTextureCoords2[] = {
            HALF, 1.0f, 0.0f, 1.0f,
            HALF, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f};
    private int[] mTextures = new int[1];

    private int mShaderProgram;
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private float[] mVideoTextureTransform = new float[16];
    private SurfaceTexture mVideoTexture;
    private MediaPlayer mMediaPlayer;
    private OnFrameEndListener mFrameEndListener;
    private String mFilePath;
    
    public VideoSprite(SpriteGLView spriteGLView, String filePath, Context context) {
        isAutoRelease = true;
        mContext = context;
        mFilePath = filePath;
        spriteGLView.runOnSurfaceViewThread(new Runnable() {
            @Override
            public void run() {
                setupGraphics();
                setupVertexBuffer();
                setupTexture();
                playVideo();
            }
        });
    }
    
    public void setOnFrameEndListener(OnFrameEndListener l) {
        mFrameEndListener = l;
    }
    
    public void playVideo() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mFrameEndListener != null) {
                        mFrameEndListener.onFrameEnd();
                    }
                }
            });
            Surface surface = new Surface(mVideoTexture);
            mMediaPlayer.setSurface(surface);
            surface.release();
            try {
                mMediaPlayer.setDataSource(mFilePath);
                mMediaPlayer.prepare();
                width = mMediaPlayer.getVideoWidth();
                height = mMediaPlayer.getVideoHeight();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mMediaPlayer.start();
        }
    }
    
    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mFrameAvailable = true;
        }
    }
    
    @Override
    public void draw(int viewWidth, int viewHeight, int attribPosition, int attribTexCoord, int mvpMatrixHandle, int textureHandle, int alphaHanle, float[] mvpMatrix) {
        GLES20.glUseProgram(mShaderProgram);
        synchronized (this) {
            if (mFrameAvailable) {
                mVideoTexture.updateTexImage();
                mVideoTexture.getTransformMatrix(mVideoTextureTransform);
                mFrameAvailable = false;
            }
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.drawTexture(viewWidth, viewHeight);
    }

    private void setupGraphics() {
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.vetext_sharder);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.fragment_sharder);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        mShaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"texture", "vPosition", "vTexCoordinate", "textureTransform"});

        GLES20.glUseProgram(mShaderProgram);
        mTextureParamHandle = GLES20.glGetUniformLocation(mShaderProgram, "texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mShaderProgram, "vTexCoordinate");
        mTextureAlphaCoordinateHandle = GLES20.glGetAttribLocation(mShaderProgram, "vTexAlphaCoordinate");
        mPositionHandle = GLES20.glGetAttribLocation(mShaderProgram, "vPosition");
        mTextureTranformHandle = GLES20.glGetUniformLocation(mShaderProgram, "textureTransform");
    }

    private void setupVertexBuffer() {
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(sDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(sDrawOrder);
        mDrawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(mSquareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mSquareCoords);
        mVertexBuffer.position(0);
    }

    private void setupTexture() {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(mTextureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        mTextureBuffer = texturebb.asFloatBuffer();
        mTextureBuffer.put(mTextureCoords);
        mTextureBuffer.position(0);
        
        ByteBuffer texturebb2 = ByteBuffer.allocateDirect(mTextureCoords2.length * 4);
        texturebb2.order(ByteOrder.nativeOrder());

        mTextureBuffer2 = texturebb2.asFloatBuffer();
        mTextureBuffer2.put(mTextureCoords2);
        mTextureBuffer2.position(0);

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, mTextures, 0);
        checkGlError("Texture generate");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[0]);
        checkGlError("Texture bind");

        mVideoTexture = new SurfaceTexture(mTextures[0]);
        mVideoTexture.setOnFrameAvailableListener(this);
    }

    private void drawTexture(int viewWidth, int viewHeight) {
        if (mVertexBuffer == null) {
            return;
        }
        float ratio = (float) viewWidth * 2 / width;
        float x = 1;
        float y = height * ratio / viewHeight;
        mSquareCoords[0] = -x; mSquareCoords[1] =  y;
        mSquareCoords[2] = -x; mSquareCoords[3] = -y;
        mSquareCoords[4] =  x; mSquareCoords[5] = -y;
        mSquareCoords[6] =  x; mSquareCoords[7] =  y;
        mVertexBuffer.put(mSquareCoords);
        mVertexBuffer.position(0);
        
        // Draw texture
        GLES20.glDisable(GLES20.GL_BLEND);
        
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mTextureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTextureAlphaCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureAlphaCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, mTextureBuffer2);

        GLES20.glUniformMatrix4fv(mTextureTranformHandle, 1, false, mVideoTextureTransform, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, sDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glDisableVertexAttribArray(mTextureAlphaCoordinateHandle);
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }
}
