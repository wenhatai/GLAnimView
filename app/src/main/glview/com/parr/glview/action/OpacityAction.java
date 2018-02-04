package com.parr.glview.action;

/**
 * 透明度改变动作
 * 
 * @author parrzhang
 *
 */
public class OpacityAction extends Action {
    private int mFromOpacity;
    private int mToOpacity;

    public OpacityAction(int duration, int fromOpacity, int toOpacity) {
        super(duration, TYPE_OPACITY, 0);
        mFromOpacity = fromOpacity;
        mToOpacity = toOpacity;
    }

    public OpacityAction(int duration, int fromOpacity, int toOpacity, int speedType) {
        super(duration, TYPE_OPACITY, speedType);
        mFromOpacity = fromOpacity;
        mToOpacity = toOpacity;
    }

    @Override
    protected void onRun(int time, float percent) {
        opacity = (int) (mFromOpacity + (mToOpacity - mFromOpacity) * percent);
        if (mToOpacity - mFromOpacity > 0) {
            if (opacity >= mToOpacity) {
                opacity = mToOpacity;
            }
        } else {
            if (opacity <= mToOpacity) {
                opacity = mToOpacity;
            }
        }
        super.onRun(time, percent);
    }
}
