package com.parr.glview.action;

public class DelayAction extends Action {

    public DelayAction(int duration) {
        super(duration, TYPE_DEALY, 0);
    }

    @Override
    protected void onRun(int time, float percent) {
        super.onRun(time, percent);
    }
}
