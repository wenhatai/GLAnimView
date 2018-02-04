package com.parr.glview.action;

/**
 * 平移动作
 * 
 * @author parrzhang
 *
 */
public class MoveToAction extends Action {
    private int mFromX;
    private int mFromY;
    private int mToX;
    private int mToY;

    public MoveToAction(int duration, int fromX, int fromY, int toX, int toY) {
        super(duration, TYPE_POSITION, 0);
        mFromX = fromX;
        mFromY = fromY;
        mToX = toX;
        mToY = toY;
    }

    public MoveToAction(int duration, int fromX, int fromY, int toX, int toY, int speedType) {
        super(duration, TYPE_POSITION, speedType);
        mFromX = fromX;
        mFromY = fromY;
        mToX = toX;
        mToY = toY;
    }

    @Override
    protected void onRun(int time, float percent) {
        x = (int) (mFromX + (mToX - mFromX) * percent);
        y = (int) (mFromY + (mToY - mFromY) * percent);
        if (mToX - mFromX > 0) {
            if (x >= mToX) {
                x = mToX;
            }
        } else {
            if (x <= mToX) {
                x = mToX;
            }
        }
        if (mToY - mFromY > 0) {
            if (y >= mToY) {
                y = mToY;
            }
        } else {
            if (y <= mToY) {
                y = mToY;
            }
        }
        super.onRun(time, percent);
    }
}
