package com.parr.glview.action;

/**
 * 缩放动作
 * 
 * @author parrzhang
 *
 */
public class ScaleAction extends Action {
    private float mFromScale;
    private float mToScale;

    public ScaleAction(int duration, float fromScale, float toScale) {
        super(duration, TYPE_SCALE, 0);
        mFromScale = fromScale;
        mToScale = toScale;
    }

    public ScaleAction(int duration, float fromScale, float toScale, int speedType) {
        super(duration, TYPE_SCALE, speedType);
        mFromScale = fromScale;
        mToScale = toScale;
    }

    @Override
    protected void onRun(int time, float percent) {
        this.scale = mFromScale + (mToScale - mFromScale) * percent;
        if (mToScale - mFromScale > 0) { // 放大
            if (scale >= mToScale) {
                scale = mToScale;
            }
        } else { // 缩小
            if (scale <= mToScale) {
                scale = mToScale;
            }
        }
        super.onRun(time, percent);
    }
}
