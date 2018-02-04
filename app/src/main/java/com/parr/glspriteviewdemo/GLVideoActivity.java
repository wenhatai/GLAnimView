package com.parr.glspriteviewdemo;

import com.parr.glspriteviewdemo.util.FileUtil;
import com.parr.glview.FrameSprite.OnFrameEndListener;
import com.parr.glview.SpriteGLView;
import com.parr.glview.VideoSprite;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

public class GLVideoActivity extends Activity {
    private SpriteGLView mSpriteGLView;
    public static final String COPYPATH = Environment.getExternalStorageDirectory()+"/newyear.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glvideo);
        mSpriteGLView = (SpriteGLView) findViewById(R.id.spriteGLView1);
        mSpriteGLView.isShowFps = true;
        File file = new File(COPYPATH);
        if(file.exists()){
            playAnim();
        }else{
            new Thread() {
                public void run() {
                    FileUtil.copyRawtoSDcard(GLVideoActivity.this,R.raw.newyear, COPYPATH);
                    mSpriteGLView.post(new Runnable() {
                        @Override
                        public void run() {
                            playAnim();
                        }
                    });
                }
            }.start();
        }

    }

    private void playAnim(){
        final VideoSprite videoSprite = new VideoSprite(mSpriteGLView, COPYPATH, GLVideoActivity.this);
        mSpriteGLView.addSprite(videoSprite);
        videoSprite.setOnFrameEndListener(new OnFrameEndListener() {
            @Override
            public void onFrameEnd() {
                mSpriteGLView.removeSprite(videoSprite);
            }
        });
    }
}
