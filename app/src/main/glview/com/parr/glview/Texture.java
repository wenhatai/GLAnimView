package com.parr.glview;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Texture {
    public static final String TAG = Texture.class.getName();
    public int textureId;
    Bitmap bitmap;

    public Texture(Context context, String fileName) {
        try {
            bitmap = BitmapFactory.decodeFile(fileName);
            init();
        } catch (OutOfMemoryError e) {
        }
    }

    public Texture(Bitmap bitmap) {
        this.bitmap = bitmap;
        init();
    }

    private void init() {
    }

    public void load() {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int textureIds[] = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        bitmap.recycle();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    public void release() {
        if (textureId != 0) {
            int[] textureIds = new int[1];
            textureIds[0] = textureId;
            GLES20.glDeleteTextures(textureIds.length, textureIds, 0);
            GLES20.glFlush();
            textureId = 0;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
