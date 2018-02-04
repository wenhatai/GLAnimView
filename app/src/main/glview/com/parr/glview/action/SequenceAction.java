package com.parr.glview.action;

/**
 * 顺序动作，将会一个接一个播放
 * 
 * @author parrzhang
 *
 */
public class SequenceAction extends Action {
    private Action mCurrentAction;
    private int mCurrentIndex = 0;
    private Action[] mActions;
    private int[] mStartTimes;

    /**
     * 连续播放的动作序列
     * @param actions
     */
    public SequenceAction(Action... actions) {
        super(0, 0, 0);
        int totalDuration = 0;
        this.mActions = actions;
        mStartTimes = new int[actions.length];
        for (int i = 0; i < actions.length; i++) {
            if (i == 0) {
                mStartTimes[i] = 0;
            } else {
                mStartTimes[i] = totalDuration;
            }
            totalDuration += actions[i].duration;
        }
        this.duration = totalDuration;
        if (actions.length > 0) {
            mCurrentAction = actions[mCurrentIndex];
        }
    }

    @Override
    protected void onRun(int time, float percent) {
        if (mCurrentIndex + 1 < mActions.length) {
            if (time > mStartTimes[mCurrentIndex + 1]) { // 轮到下一个动作
                if (mCurrentAction.isRepeat) {
                    mCurrentAction.start();
                } else {
                    mCurrentAction.stop();
                    mCurrentAction = mActions[++mCurrentIndex];
                }
            }
        }
        type = mCurrentAction.type;
        int currentTime = time - mStartTimes[mCurrentIndex];
        float percentCurrent = (float) currentTime / (float) mCurrentAction.duration;
        if (mCurrentAction.speedType == SPEED_TYPE_ACCELERATE) {
            percentCurrent = (float) (currentTime * currentTime) / (float) (mCurrentAction.duration * mCurrentAction.duration);
        } else if (mCurrentAction.speedType == SPEED_TYPE_REDUCE) {
            float t_T = (float) currentTime / mCurrentAction.duration;
            percentCurrent = t_T * (2 - t_T);
        }
        mCurrentAction.onRun(currentTime, percentCurrent);
        if ((mCurrentAction.type & Action.TYPE_POSITION) != 0) {
            x = mCurrentAction.x;
            y = mCurrentAction.y;
        }
        if ((mCurrentAction.type & Action.TYPE_SCALE) != 0) {
            scale = mCurrentAction.scale;
        }
        if ((mCurrentAction.type & Action.TYPE_OPACITY) != 0) {
            opacity = mCurrentAction.opacity;
        }
        if ((mCurrentAction.type & Action.TYPE_ROTATE) != 0) {
            rotate = mCurrentAction.rotate;
        }
        if (mCurrentAction.isEnd && mCurrentAction.isRepeat) {
            mCurrentAction.start();
        }
        super.onRun(time, percent);
    }
    
    @Override
    public void start() {
        super.start();
        mCurrentIndex = 0;
        if (mActions.length > 0) {
            mCurrentAction = mActions[mCurrentIndex];
        }
    }
}
