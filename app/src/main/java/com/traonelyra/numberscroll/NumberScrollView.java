package com.traonelyra.numberscroll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NumberScrollView extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Rect mTextRect = new Rect();
    private final float mMaxMoveHeight;
    private float mCurrentMoveHeight;
    private float mOutterMoveHeight;
    private float mCurrentAlphaValue;

    private final List<String> mTargetNumberList = new ArrayList<>();
    private final List<String> mStartNumberList = new ArrayList<>();
    private final List<String> mNextNumberList = new ArrayList<>();
    private final List<String> mCurrentNumberList = new ArrayList<>();

    private float mMaxNumWidth = 0;

    public NumberScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint.setColor(Color.BLACK);

        int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 54, context.getResources().getDisplayMetrics());
        mPaint.setTextSize(fontSize);

        for (int i = 0; i <= 9; i++) {
            float width = mPaint.measureText(String.valueOf(i));
            mPaint.getTextBounds(String.valueOf(i), 0, 1, mTextRect);
            mMaxNumWidth = Math.max(width, mTextRect.width());
        }

        mMaxMoveHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        float startX = 0;
        float startY;
        for (int i = mNextNumberList.size() - 1; i >= 0; i--) {
            if (mNextNumberList.get(i).equals(mCurrentNumberList.get(i))) {
                mPaint.setColor(Color.BLACK);
                mPaint.setAlpha(255);
                mPaint.getTextBounds(mNextNumberList.get(i), 0, mNextNumberList.get(i).length(), mTextRect);
                startY = (float) getHeight() / 2 + (float) mTextRect.height() / 2;
                canvas.drawText(mNextNumberList.get(i), startX + (mMaxNumWidth - mTextRect.width()) / 2, startY, mPaint);
            } else {
                mPaint.setAlpha((int) (255 * (1 - mCurrentAlphaValue)));
                mPaint.getTextBounds(mCurrentNumberList.get(i), 0, mNextNumberList.get(i).length(), mTextRect);
                startY = (float) getHeight() / 2 + (float) mTextRect.height() / 2;
                canvas.drawText(mCurrentNumberList.get(i), startX + (mMaxNumWidth - mTextRect.width()) / 2, startY + mOutterMoveHeight, mPaint);
                mPaint.setAlpha((int) (255 * mCurrentAlphaValue));
                mPaint.getTextBounds(mNextNumberList.get(i), 0, mNextNumberList.get(i).length(), mTextRect);
                startY = (float) getHeight() / 2 + (float) mTextRect.height() / 2;
                canvas.drawText(mNextNumberList.get(i), startX + (mMaxNumWidth - mTextRect.width()) / 2, startY + mCurrentMoveHeight, mPaint);
            }
            startX += mMaxNumWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension((int) (mMaxNumWidth * mTargetNumberList.size()), heightSize);
    }

    public void setContent(String startNumber, String targetNumber) {
        if (startNumber.length() > targetNumber.length()) {
            // 只允许增加
            return;
        }
        prepareNumberLists(startNumber, targetNumber);
        startAnimator();
    }

    /**
     * 启动动画
     */
    private void startAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(animation -> {
            float currentValue = (float) animation.getAnimatedValue();
            mCurrentMoveHeight = mMaxMoveHeight * (1 - currentValue);
            mCurrentAlphaValue = currentValue;
            mOutterMoveHeight = -mMaxMoveHeight * currentValue;
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!isListEqual(mTargetNumberList, mNextNumberList)) {
                    generateNextNumberList();
                    animator.start();
                }
            }
        });
        animator.setDuration(100);
        animator.start();
    }

    /**
     * 生成数字列表
     */
    private void prepareNumberLists(String startNumber, String targetNumber) {
        mTargetNumberList.clear();
        mStartNumberList.clear();

        for (int i = startNumber.length() - 1; i >= 0; i--) {
            mStartNumberList.add(String.valueOf(startNumber.charAt(i)));
        }

        for (int i = targetNumber.length() - 1; i >= 0; i--) {
            mTargetNumberList.add(String.valueOf(targetNumber.charAt(i)));
        }

        while (mStartNumberList.size() < mTargetNumberList.size()) {
            mStartNumberList.add(" ");
        }

        mNextNumberList.clear();
        mNextNumberList.addAll(mStartNumberList);

        generateNextNumberList();
    }

    /**
     * 获取下一个数字列表
     */
    private void generateNextNumberList() {
        mCurrentNumberList.clear();
        mCurrentNumberList.addAll(mNextNumberList);
        mNextNumberList.clear();
        boolean hasChanged = false;
        for (int i = 0; i < mTargetNumberList.size(); i++) {
            if (hasChanged) {
                mNextNumberList.add(mCurrentNumberList.get(i));
            } else {
                if (TextUtils.equals(mCurrentNumberList.get(i), mTargetNumberList.get(i))) {
                    mNextNumberList.add(mCurrentNumberList.get(i));
                } else {
                    hasChanged = true;
                    mNextNumberList.add(getNextNumber(mCurrentNumberList.get(i)));
                }
            }
        }
    }

    /**
     * 获取下一个数字
     */
    private String getNextNumber(String current) {
        switch (current) {
            case "0":
                return "1";
            case "1":
                return "2";
            case "2":
                return "3";
            case "3":
                return "4";
            case "4":
                return "5";
            case "5":
                return "6";
            case "6":
                return "7";
            case "7":
                return "8";
            case "8":
                return "9";
            case "9":
            case " ":
                return "0";
            default:
                return current;
        }
    }

    /**
     * 判断两个列表是否相等
     */
    private boolean isListEqual(List<String> aList, List<String> bList) {
        if (aList.size() != bList.size()) {
            return false;
        }
        for (int i = 0; i < aList.size(); i++) {
            if (!TextUtils.equals(aList.get(i), bList.get(i))) {
                return false;
            }
        }
        return true;
    }
}
