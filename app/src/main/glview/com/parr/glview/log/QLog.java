package com.parr.glview.log;

import android.util.Log;

/**
 * @author parrzhang
 *
 */
public class QLog {
    public static final String ERR_KEY = "qq_error|";
    public static final int DEV = 4;
    public static final int CLR = 2;
    public static final int USR = 1;

    /**
     * 判断是否染色级别
     * 
     * @return
     */
    public static boolean isColorLevel() {
        return true;
    }

    public static boolean isDevelopLevel() {
        return false;
    }

    /**
     * 输出日志
     * 
     * @param tag
     * @param level
     * @param msg
     */
    public static void i(String tag, int level, String msg) {
        Log.i(tag, msg);
    }

    public static void d(String tag, int level, String msg) {
        Log.d(tag, msg);
    }
}
