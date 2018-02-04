package com.parr.glview.action;

public abstract class Action {
    /** 延时动作 */
    public static final int TYPE_DEALY = 0x00;
    /** 改变位置 */
    public static final int TYPE_POSITION = 0x01;
    /** 改变缩放比 */
    public static final int TYPE_SCALE = 0x02;
    /** 改变透明度 */
    public static final int TYPE_OPACITY = 0x04;
    /** 改变旋转角度 */
    public static final int TYPE_ROTATE = 0x08;

    /** 匀速 */
    public static final int SPEED_TYPE_UNIFORM = 0;
    /** 加速 */
    public static final int SPEED_TYPE_ACCELERATE = 1;
    /** 减速 */
    public static final int SPEED_TYPE_REDUCE = 2;

    public int x = 0;
    public int y = 0;
    public float scale = 1;
    public int opacity = 255;
    public int rotate = 0;
    public int duration;
    /** 动作的类型，可以是多种动作的组合 */
    public int type;
    public int speedType = SPEED_TYPE_UNIFORM;
    public boolean isRepeat = false;
    public boolean isEnd = false;
    protected long startTime;
    private OnActionEndListener listener;

    /**
     * 运行时
     * 
     * @param time
     *            过去的时间
     * @param percent
     *            变化的百分比，带有加速权重
     */
    protected void onRun(int time, float percent) {
        if (time >= duration && listener != null) {
            listener.onActionEnd();
        }
    }

    public Action(int duration, int type, int speedType) {
        this.duration = duration;
        this.type = type;
        this.speedType = speedType;
    }

    /**
     * 动作执行
     * 
     * @return
     */
    public boolean run() {
        if (isEnd) {
            return false;
        }
        int time = (int) (System.currentTimeMillis() - startTime);
        float percent = (float) time / (float) duration;
        if (speedType == SPEED_TYPE_ACCELERATE) {
            percent = (float) (time * time) / (float) (duration * duration);
        } else if (speedType == SPEED_TYPE_REDUCE) {
            float t_T = (float) time / duration;
            percent = t_T * (2 - t_T);
        }
        onRun(time, percent);
        if (time >= duration) {
            stop();
        }
        return true;
    }

    /**
     * 开始动作，记录时间
     */
    public void start() {
        isEnd = false;
        startTime = System.currentTimeMillis();
    }

    /**
     * 动作停止
     */
    public void stop() {
        isEnd = true;
    }

    /**
     * 动作完成监听
     * 
     * @param listener
     */
    public void setOnActionListener(OnActionEndListener listener) {
        this.listener = listener;
    }

    public static interface OnActionEndListener {
        /**
         * 动作停止
         */
        public void onActionEnd();
    }
}
