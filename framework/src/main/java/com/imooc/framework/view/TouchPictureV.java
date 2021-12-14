package com.imooc.framework.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.imooc.framework.R;

public class TouchPictureV extends View {

    // 背景
    private Bitmap bgBitmap;
    // 背景画笔
    private Paint mPaintbg;

    // 空白块
    private Bitmap mNullBitmap;
    // 空白块画笔
    private Paint mPaintNull;

    // 移动方块
    private Bitmap mMoveBitmap;
    // 移动画笔
    private Paint mPaintMove;

    private int mWidth;
    private int mHeight;

    // 方块大小
    private static int CARD_SIZE = 200;
    // 方块坐标
    private static int LINE_W, LINE_H = 0;

    // 移动方块横坐标
    private static int moveX = 200;

    // 误差值
    private static final int errorValue = 10;

    private OnViewResultListener viewResultListener;

    public void setViewResultListener(OnViewResultListener viewResultListener) {
        this.viewResultListener = viewResultListener;
    }

    public TouchPictureV(Context context) {
        super(context);
        init();
    }

    public TouchPictureV(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPictureV(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintbg = new Paint();
        mPaintNull = new Paint();
        mPaintMove = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBg(canvas);
        drawNullCard(canvas);
        drawMoveCard(canvas);
    }

    /**
     * 绘制背景
     *
     * @param canvas 画布
     */
    private void drawBg(Canvas canvas) {
        // 获取图片
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_bg);
        // 创建一个空的Bitmap
        bgBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        // 将图片绘制到空的bitmap
        Canvas bgCanvas = new Canvas(bgBitmap);
        bgCanvas.drawBitmap(mBitmap, null, new Rect(0, 0, mWidth, mHeight), mPaintbg);

        // 将bgBitmap绘制到View上
        canvas.drawBitmap(bgBitmap, null, new Rect(0, 0, mWidth, mHeight), mPaintbg);
    }

    /**
     * 绘制空白块
     *
     * @param canvas 画布
     */
    private void drawNullCard(Canvas canvas) {
        mNullBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_null_card);
        CARD_SIZE = mNullBitmap.getWidth();

        LINE_W = mWidth / 3 * 2;
        LINE_H = mHeight / 2 - (CARD_SIZE / 2);

        canvas.drawBitmap(mNullBitmap, LINE_W, LINE_H, mPaintNull);
    }

    /**
     * 绘制空白块
     *
     * @param canvas 画布
     */
    private void drawMoveCard(Canvas canvas) {
        // 截取空白块位置坐标的Bitmap图像
        mMoveBitmap = Bitmap.createBitmap(bgBitmap, LINE_W, LINE_H, CARD_SIZE, CARD_SIZE);
        // 绘制在View上
        canvas.drawBitmap(mMoveBitmap, moveX, LINE_H, mPaintMove);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (event.getX() > 0 && event.getX() < (mWidth - CARD_SIZE)) {
                    moveX = (int) event.getX();
                    invalidate();

                }
                break;
            case MotionEvent.ACTION_UP:
                if (moveX > (LINE_W - errorValue) && moveX < (LINE_W + errorValue)) {
                    if (viewResultListener != null) {
                        viewResultListener.onResult();

                    }
                }
                moveX = 200;
                invalidate();
                break;
        }
        return true;
    }

    public interface OnViewResultListener {
        void onResult();
    }
}
