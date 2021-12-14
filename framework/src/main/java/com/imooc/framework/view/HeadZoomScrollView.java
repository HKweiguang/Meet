package com.imooc.framework.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class HeadZoomScrollView extends ScrollView {

    // 头部View
    private View mZoomView;
    private int mZoomViewWidth;
    private int mZoomViewHeight;
    // 是否在滑动
    private boolean isScrolling = false;
    // 第一次按下的坐标
    private float firstPosition;
    // 滑动系数
    private static final float mScrollRate = 0.3f;
    // 回弹系数
    private static final float mReplyRate = 0.5f;

    public HeadZoomScrollView(Context context) {
        super(context);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildAt(0) != null) {
            ViewGroup vg = (ViewGroup) getChildAt(0);
            if (vg.getChildAt(0) != null) {
                mZoomView = vg.getChildAt(0);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 获取View的宽高
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            mZoomViewWidth = mZoomView.getMeasuredWidth();
            mZoomViewHeight = mZoomView.getMeasuredHeight();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!isScrolling) {
                    if (getScrollY() == 0) {
                        firstPosition = ev.getY();
                    } else {
                        break;
                    }
                }

                int distance = (int) ((ev.getY() - firstPosition) * mScrollRate);
                if (distance < 0) {
                    break;
                }
                isScrolling = true;
                setZoomView(distance);
                break;
            case MotionEvent.ACTION_UP:
                isScrolling = false;
                replyZoomView();
                break;
        }
        return true;
    }

    /**
     * 缩放View
     */
    private void setZoomView(float zoom) {
        // 获取View的宽高
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            return;
        }
        ViewGroup.LayoutParams lp = mZoomView.getLayoutParams();
        lp.width = (int) (mZoomViewWidth + zoom);
        lp.height = (int) (mZoomViewHeight * ((mZoomViewWidth + zoom) / mZoomViewWidth));
        ((MarginLayoutParams) lp).setMargins((int) -(zoom / 2), 0, 0, 0);
        mZoomView.setLayoutParams(lp);
    }

    /**
     * 回弹动画
     */
    private void replyZoomView() {
        int distance = mZoomView.getMeasuredWidth() - mZoomViewWidth;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(distance, 0).setDuration((long) (distance * mReplyRate));
        valueAnimator.addUpdateListener(animation -> setZoomView((Float) animation.getAnimatedValue()));
        valueAnimator.start();
    }
}
