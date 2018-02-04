package com.parr.glview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.parr.glview.action.Action;

public class Sprite {
    public static final String TAG = Sprite.class.getName();
    public float width;
    public float height;
    public float x;
    public float y;
    public float scale = 1;
    public float rotate = 0;
    public int opacity = 255;
    public boolean isAutoRelease = false;
    
    protected SpriteGLView mSpriteGLView;
    protected List<Action> mActions = new ArrayList<Action>();
    protected Texture mTexture;
    
    private final float[] VERTEX = {
            1,  1,  0,   // top right
           -1,  1,  0,  // top left
           -1, -1,  0, // bottom left
            1, -1,  0,  // bottom right
   };
   protected final short[] VERTEX_INDEX = { 0, 1, 2, 2, 0, 3 };
   protected final float[] UV_TEX_VERTEX = {
           1,  0,  // bottom right
           0,  0,  // bottom left
           0,  1,  // top left
           1,  1,  // top right
   };

   protected FloatBuffer mVertexBuffer;
   protected ShortBuffer mVertexIndexBuffer;
   protected FloatBuffer mUvTexVertexBuffer;
   
   protected float[] modelMatrix = new float[16];
   
    public Sprite(SpriteGLView spriteGLView, Context context, String fileName) {
        mSpriteGLView = spriteGLView;
        mTexture = new Texture(context, fileName);
        init();
        initDatas();
    }
    
    public Sprite(SpriteGLView spriteGLView, Bitmap bitmap) {
        mSpriteGLView = spriteGLView;
        mTexture = new Texture(bitmap);
        init();
        initDatas();
    }
    
    public Sprite(SpriteGLView spriteGLView) {
        mSpriteGLView = spriteGLView;
        initDatas();
    }
    
    public Sprite() {
        initDatas();
    }
    
    public void setBitmap(Bitmap bitmap) {
        mTexture = new Texture(bitmap);
        init();
    }
    
    public void setTexture(Texture texture) {
        mTexture = texture;
        init();
    }
    
    private void initDatas() {
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);

        mUvTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(UV_TEX_VERTEX);
        mUvTexVertexBuffer.position(0);
    }
    
    protected void init() {
        if (mTexture == null || mTexture.bitmap == null) {
            return;
        }
        width = mTexture.bitmap.getWidth();
        height = mTexture.bitmap.getHeight();
    }

    /**
     * 播放动作，可以同时添加多个动作，将会同时进行，每个动作可以有自己的延时时间，调用该方法会移除之前所有的动作
     * 
     * @param actions
     *            动作序列
     */
    public void runAction(Action... actions) {
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            action.start();
            mActions.add(action);
        }
    }

    /**
     * 停止所有的动作
     */
    public void stopActions() {
        mActions.clear();
    }
    
    public void draw(int viewWidth, int viewHeight, int attribPosition, int attribTexCoord, int mvpMatrixHandle, int textureHandle, int alphaHanle, float[] mvpMatrix) {
        if (mTexture == null) {
            return;
        }
        for (int i = 0; i < mActions.size(); i++) {
            Action action = mActions.get(i);
            if (action.run()) {
                if ((action.type & Action.TYPE_POSITION) != 0) {
                    x = action.x;
                    y = action.y;
                }
                if ((action.type & Action.TYPE_SCALE) != 0) {
                    scale = action.scale;
                }
                if ((action.type & Action.TYPE_OPACITY) != 0) {
                    opacity = action.opacity;
                }
                if ((action.type & Action.TYPE_ROTATE) != 0) {
                    rotate = action.rotate;
                }
            }
            if (action.isEnd) {
                if (action.isRepeat) {
                    action.start();
                } else {
                    mActions.remove(i);
                    i--;
                }
            }
        }
        int textureId = getTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glVertexAttribPointer(attribPosition, 3, GLES20.GL_FLOAT, false, 0,
                mVertexBuffer);

        GLES20.glEnableVertexAttribArray(attribTexCoord);
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 0,
                mUvTexVertexBuffer);
        GLES20.glUniform1i(mSpriteGLView.isVideoHandle, 0);
        
        Matrix.setIdentityM(modelMatrix, 0);
        // 缩放
        float ratio = (float) viewWidth / viewHeight;
        float w = viewWidth / width / scale;
        Matrix.translateM(modelMatrix, 0, (2 * x) / viewHeight,  (2 * y) / viewHeight, 0);
        Matrix.rotateM(modelMatrix, 0, -rotate, 0, 0, 1);
        Matrix.scaleM(modelMatrix, 0,  ratio / w,  ratio / w * (height / width), 1);
        float[] objMatrix = new float[16];
        Matrix.multiplyMM(objMatrix, 0, mvpMatrix, 0, modelMatrix, 0);

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, objMatrix, 0);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glUniform1f(alphaHanle, opacity / 255.0f);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);

        GLES20.glDisableVertexAttribArray(attribPosition);
        GLES20.glDisableVertexAttribArray(attribTexCoord);
    }
    
    public int getTextureId() {
    	mTexture.load();
    	return mTexture.textureId;
    }
    
    public void release() {
        if (mTexture != null) {
            mTexture.release();
        }
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 获得宽度
     * @return
     */
    public int getWidth() {
        if (mTexture != null && mTexture.bitmap == null) {
            return 0;
        }
        return mTexture.bitmap.getWidth();
    }
    
    /**
     * 获得高度
     * @return
     */
    public int getHeight() {
        if (mTexture != null && mTexture.bitmap == null) {
            return 0;
        }
        return mTexture.bitmap.getHeight();
    }
    
    interface Clickable {
        public boolean onTouchEvent(MotionEvent event, int width, int height);
    }
}
