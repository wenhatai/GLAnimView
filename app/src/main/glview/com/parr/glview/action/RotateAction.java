package com.parr.glview.action;

/**
 * 旋转动作
 * 
 * @author parrzhang
 *
 */
public class RotateAction extends Action {
    private int mFromRotate;
    private int mToRotate;

    public RotateAction(int duration, int fromRotate, int toRotate) {
        super(duration, TYPE_ROTATE, 0);
        mFromRotate = fromRotate;
        mToRotate = toRotate;
    }

    public RotateAction(int duration, int fromRotate, int toRotate, int speedType) {
        super(duration, TYPE_ROTATE, speedType);
        mFromRotate = fromRotate;
        mToRotate = toRotate;
    }

    @Override
    protected void onRun(int time, float percent) {
        System.out.println("time = " + time + ", percent = " + percent);
        rotate = (int) (mFromRotate + (mToRotate - mFromRotate) * percent);
        if (mToRotate - mFromRotate > 0) {
            if (rotate >= mToRotate) {
                rotate = mToRotate;
            }
        } else {
            if (rotate <= mToRotate) {
                rotate = mToRotate;
            }
        }
        super.onRun(time, percent);
    }
}
