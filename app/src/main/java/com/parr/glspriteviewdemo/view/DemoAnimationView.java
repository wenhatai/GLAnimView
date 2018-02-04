package com.parr.glspriteviewdemo.view;

import java.util.Random;

import com.parr.glspriteviewdemo.R;
import com.parr.glview.Sprite;
import com.parr.glview.SpriteGLView;
import com.parr.glview.Texture;
import com.parr.glview.action.Action.OnActionEndListener;
import com.parr.glview.action.OpacityAction;
import com.parr.glview.action.RotateAction;
import com.parr.glview.util.DisplayUtil;
import com.parr.glview.action.DelayAction;
import com.parr.glview.action.MoveToAction;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;

public class DemoAnimationView extends SpriteGLView implements Runnable {
    private Texture[] mTextures = new Texture[16];

    public DemoAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isShowFps = true;
        setBackgroundColor(Color.parseColor("#ff9999"));
    }

    public void init() {
        for (int i = 0; i < mTextures.length; i++) {
            mTextures[i] = new Texture(BitmapFactory.decodeResource(getResources(), R.mipmap.demo01 + i));
        }
        runOnSurfaceViewThread(this);
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 6; i++) {
            final Sprite sprite = new Sprite(this);
            Random random = new Random();
            sprite.setTexture(mTextures[random.nextInt(16)]);
            sprite.scale = (random.nextInt(3) + 8) / 10.0f;
            int y = getHeight() / 2 - getHeight() * (i + 3) / 11;
            y += random.nextInt(DisplayUtil.dip2px(getContext(), 20)) - 10;
            sprite.setPosition(getWidth() / 2 + sprite.width / 2 + random.nextInt(DisplayUtil.dip2px(getContext(), 100)), y);
            MoveToAction action = new MoveToAction(3000, (int) sprite.x, (int) sprite.y, -getWidth() -sprite.getWidth(), y);
            RotateAction rotateAction = new RotateAction(1000,0,random.nextInt(3)*180);
            OpacityAction opacityAction = new OpacityAction(2000,random.nextInt(200),255);

            sprite.runAction(action,rotateAction,opacityAction);
            action.setOnActionListener(new OnActionEndListener() {
                @Override
                public void onActionEnd() {
                    removeSprite(sprite);
                }
            });
            if (i == 0) {
                DelayAction delayAction = new DelayAction(500);
                sprite.runAction(delayAction);
                delayAction.setOnActionListener(new OnActionEndListener() {
                    @Override
                    public void onActionEnd() {
                        runOnSurfaceViewThread(DemoAnimationView.this);
                    }
                });
            }
            addSprite(sprite);
        }
    }
}
