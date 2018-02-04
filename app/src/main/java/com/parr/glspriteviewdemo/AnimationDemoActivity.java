package com.parr.glspriteviewdemo;

import com.parr.glspriteviewdemo.view.DemoAnimationView;

import android.app.Activity;
import android.os.Bundle;

public class AnimationDemoActivity extends Activity {

private DemoAnimationView mDemoAnimationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_demo);
        
        mDemoAnimationView = (DemoAnimationView) findViewById(R.id.spriteGLView1);
        mDemoAnimationView.post(new Runnable() {
            @Override
            public void run() {
                mDemoAnimationView.init();
            }
        });
    }
    
    @Override
        protected void onDestroy() {
            super.onDestroy();
            mDemoAnimationView.stopDraw();
        }
}
