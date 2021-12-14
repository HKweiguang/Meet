package com.imooc.framework.utils;

import android.graphics.Color;

import androidx.annotation.ColorRes;

public class ColorUtils {

    /**
     * 判断深色or浅色
     *
     * @param color 颜色
     * @return true-浅色，false-深色
     */
    public static boolean isLightColor(@ColorRes int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }
}
