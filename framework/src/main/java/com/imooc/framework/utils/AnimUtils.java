package com.imooc.framework.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class AnimUtils {

    /**
     * 旋转动画
     *
     * @param view 旋转的控件
     * @return ObjectAnimator
     */
    public static ObjectAnimator rotation(View view) {
        ObjectAnimator mAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        mAnim.setDuration(2 * 1000);
        mAnim.setRepeatMode(ValueAnimator.RESTART);
        mAnim.setRepeatCount(ValueAnimator.INFINITE);
        mAnim.setInterpolator(new LinearInterpolator());

        return mAnim;
    }

}
