package com.rdc.liyuzhen.sidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 侧边菜单
 */
public class SideMenu extends View {
    private static final String TAG = "SideMenu";
    // view自身高度
    private int viewHeight = 0;
    // view自身宽度
    private int viewWidth = 0;

    // 内圆半径
    private float innerCircleRadius;
    // 外圆半径
    private float outerCircleRadius;
    // 内圆背景颜色
    private int innerCircleColor;
    // 外圆背景颜色
    private int outerCircleColor;
    // 未完成条目的颜色
    private int unfinishedColor;
    // 已完成条目的颜色
    private int finishedTextColor;
    // 竖直条目间距
    private float verticalSpacing;
    // 竖线宽度
    private float verticalLineWidth;
    // 序号文本大小
    private float indexTextSize;
    // 菜单文本大小
    private float menuNameTextSize;
    // 菜单文本左边距
    private float menuNameMarginLeft;
    // 序号文本颜色
    private int indexTextColor;
    // 内外圆半径之差
    private float offsetValue;

    // 标记是否第一次计算
    private boolean firstCal = true;
    // 标记当前点击的位标
    private int clickIndex = 0;
    // 记录down触摸事件时Touch的位标
    private int tempDownIndex = -1;

    private Paint mPaint;

    private ArrayList<String> mMenuNames;
    private List<RectF> mItemClickAreaList;
    private OnMenuItemClickListener mOnMenuItemClickListener;

    public SideMenu(Context context) {
        super(context);
        init();
    }

    public SideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SideMenu);

        innerCircleRadius = typedArray.getDimension(R.styleable.SideMenu_innerCircleRadius, 10.0f);
        innerCircleColor = typedArray.getColor(R.styleable.SideMenu_innerCircleColor, 0);
        outerCircleColor = typedArray.getColor(R.styleable.SideMenu_outerCircleColor, 0);
        unfinishedColor = typedArray.getColor(R.styleable.SideMenu_unfinishedTextColor, 0);
        finishedTextColor = typedArray.getColor(R.styleable.SideMenu_finishedTextColor, 0);
        verticalSpacing = typedArray.getDimension(R.styleable.SideMenu_verticalSpacing, 0);
        offsetValue = typedArray.getDimension(R.styleable.SideMenu_innerOuterOffsetValue, 0);
        indexTextSize = typedArray.getDimension(R.styleable.SideMenu_indexTextSize, 0);
        menuNameTextSize = typedArray.getDimension(R.styleable.SideMenu_menuNameTextSize, 0);
        verticalLineWidth = typedArray.getDimension(R.styleable.SideMenu_verticalLineWidth, 0);
        menuNameMarginLeft = typedArray.getDimension(R.styleable.SideMenu_menuNameMarginLeft, 0);
        indexTextColor = typedArray.getColor(R.styleable.SideMenu_indexTextColor, 0);

        outerCircleRadius = innerCircleRadius + offsetValue;

        typedArray.recycle();
    }

    /**
     * 初始化部分数据
     */
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mMenuNames = new ArrayList<>();
        mMenuNames.add("Default Value");
        mMenuNames.add("Default Value");
        mMenuNames.add("Default Value");
        mMenuNames.add("Default Value");
        mMenuNames.add("Default Value");
        mItemClickAreaList = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (viewWidth != 0 && viewHeight != 0) {
            int exactlyWidth, exactlyHeight;
            exactlyWidth = measureWidth(widthMeasureSpec);
            exactlyHeight = measureHeight(heightMeasureSpec);
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(exactlyWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(exactlyHeight, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 获取正确view高度
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode ==MeasureSpec.UNSPECIFIED) {
            return viewHeight;
        } else {
            return heightSize;
        }
    }

    /**
     * 获取正确view宽度
     * @param widthMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        if (widthMode ==MeasureSpec.UNSPECIFIED) {
            return viewWidth;
        } else {
            return widthSize;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mMenuNames == null || mMenuNames.size() == 0)
            return;

        drawCircleAndIndex(canvas);
        drawVerticalLine(canvas);
        drawMenuNames(canvas);
        buildClickArea();
    }

    /**
     * 建立可点击区域
     */
    private void buildClickArea() {
        if (!firstCal) return;
        firstCal = false;

        float centerY, menuNameWidth;
        for (int i = 0; i < mMenuNames.size(); i++) {
            centerY = offsetValue + 2 * innerCircleRadius * i + verticalSpacing * i + innerCircleRadius;

            RectF rectf = new RectF();
            rectf.left = getPaddingLeft();
            rectf.top = centerY - outerCircleRadius + getPaddingTop();
            rectf.bottom = centerY + outerCircleRadius + getPaddingTop();
            menuNameWidth = mPaint.measureText(mMenuNames.get(i));
            rectf.right = 2 * outerCircleRadius + menuNameMarginLeft + menuNameWidth + getPaddingLeft();

            mItemClickAreaList.add(rectf);

            calViewWidth(rectf.right);
        }
        requestLayout();
    }

    /**
     * 计算view宽度
     */
    private void calViewWidth(float right) {
        // 此处加多一个offsetValue为了保持对称
        float width = right + getPaddingRight() + offsetValue;
        if (width > viewWidth) {
            viewWidth = (int) width;
        }
    }

    /**
     * 计算view高度
     */
    private void calViewHeight() {
        int size = mMenuNames.size();
        viewHeight = (int) (2 * offsetValue + size * innerCircleRadius * 2 + (size - 1) * verticalSpacing
                        + getPaddingTop() + getPaddingBottom());
    }

    /**
     * 绘制圆形加中心文本
     *
     * @param canvas 要绘制上去的Canvas（画板）
     */
    private void drawCircleAndIndex(Canvas canvas) {
        float centerX, centerY, indexX, indexY;
        for (int i = 0; i < mMenuNames.size(); i++) {
            centerX = offsetValue + innerCircleRadius + getPaddingLeft();
            centerY = offsetValue + 2 * innerCircleRadius * i + verticalSpacing * i + innerCircleRadius + getPaddingTop();

            // 绘制圆形
            mPaint.setStyle(Paint.Style.FILL);
            if (i <= clickIndex) {
                mPaint.setColor(innerCircleColor);
            } else {
                mPaint.setColor(unfinishedColor);
            }
            canvas.drawCircle(centerX, centerY, innerCircleRadius, mPaint);

            // 绘制圆环
            if (clickIndex == i) {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(offsetValue);
                mPaint.setColor(outerCircleColor);

                // 加或减圆环宽度的一半是因为，left = centerX - outerCircleRadius，而left的位置是stroke的中心位置，不是stroke的外边界位置
                RectF rect = new RectF(centerX - outerCircleRadius + offsetValue / 2, centerY - outerCircleRadius + offsetValue / 2
                        , centerX + outerCircleRadius - offsetValue / 2, centerY + outerCircleRadius - offsetValue / 2);
                canvas.drawArc(rect, 0, 360, false, mPaint);
            }

            // 绘制序号
            mPaint.setColor(indexTextColor);
            mPaint.setTextSize(indexTextSize);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setFakeBoldText(false);

            String index = (i + 1) + "";
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            indexX = centerX - mPaint.measureText(index) / 2;
            indexY = centerY + (-fontMetrics.ascent - (fontMetrics.descent - fontMetrics.ascent) / 2.0f);
            canvas.drawText(index, indexX, indexY, mPaint);
        }
    }

    /**
     * 绘制竖线
     *
     * @param canvas 要绘制上去的Canvas（画板）
     */
    private void drawVerticalLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);

        float left, top, bottom;
        for (int i = 0; i < mMenuNames.size() - 1; i++) {
            if (i <= clickIndex - 1) {
                mPaint.setColor(innerCircleColor);
            } else {
                mPaint.setColor(unfinishedColor);
            }

            left = outerCircleRadius - verticalLineWidth / 2 + getPaddingLeft();  // 保持竖线位于中心位置

            top = offsetValue + 2 * innerCircleRadius * (i + 1) + verticalSpacing * i + getPaddingTop();
            bottom = top + verticalSpacing;
            /**
             * 此处判断是因为，当遍历到i等于clickIndex的时候，直接绘制，将会导致绘制的灰色竖线遮挡
             * 到圆环，所以当i等于clickIndex时，竖线长度应缩短offsetValue，竖线top位置下移一个offsetValue
             */
            if (i == clickIndex) {
                top = top + offsetValue;
            }

            canvas.drawRect(left, top, left + verticalLineWidth, bottom, mPaint);
        }
    }

    /**
     * 绘制菜单文本
     *
     * @param canvas 要绘制上去的Canvas（画板）
     */
    private void drawMenuNames(Canvas canvas) {
        mPaint.setTextSize(menuNameTextSize);
        mPaint.setFakeBoldText(true);  // 设置粗体
        float x, y;

        for (int i = 0; i < mMenuNames.size(); i++) {
            if (i < clickIndex) {
                mPaint.setColor(finishedTextColor);
            } else if (i == clickIndex) {
                mPaint.setColor(innerCircleColor);
            } else {
                mPaint.setColor(unfinishedColor);
            }

            String menuName = mMenuNames.get(i);
            // 计算文本高度
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

            // 确定绘制起点在竖直中心位置
            x = 2 * outerCircleRadius + menuNameMarginLeft + getPaddingLeft();
            y = (offsetValue + 2 * innerCircleRadius * i + verticalSpacing * i + innerCircleRadius) +
                    (-fontMetrics.ascent - (fontMetrics.descent - fontMetrics.ascent) / 2.0f) + getPaddingTop();

            canvas.drawText(menuName, x, y, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 这里计算出两个触摸事件的位标，当两个位标相同时才触发“点击”事件并回调
         * （刚开始我在UP触摸事件直接判断位标，然后回调，实质上并不是“点击”操作触发的事件）
         */
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tempDownIndex = checkClickItem(event);
                break;
            case MotionEvent.ACTION_UP:
                int index = checkClickItem(event);
                if (index != -1 && index == tempDownIndex && index != clickIndex) {
                    clickIndex = index;
                    invalidate();

                    // 回调
                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.onMenuItemClick(clickIndex);
                    }
                }
                tempDownIndex = -1;
                break;
        }
        return true;
    }

    /**
     * 检查点击的条目，并重绘
     */
    private int checkClickItem(MotionEvent event) {
        float pointX, pointY;
        pointX = event.getX();
        pointY = event.getY();
        for (int i = 0; i < mItemClickAreaList.size(); i++) {
            RectF area = mItemClickAreaList.get(i);

            // 判断是否在哪个点击区域内
            if (pointX >= area.left && pointX < area.right && pointY >= area.top && pointY < area.bottom) {
                return i;
            }
        }
        return -1;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(int index);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        mOnMenuItemClickListener = onMenuItemClickListener;
    }

    /**
     * 设置每个菜单名字
     *
     * @param menuNames 所有菜单的名字
     */
    public void setMenuItem(List<String> menuNames) {
        if (mMenuNames == null) {
            mMenuNames = new ArrayList<>();
            mItemClickAreaList = new ArrayList<>();
        } else {
            mMenuNames.clear();
            mItemClickAreaList.clear();
        }
        mMenuNames.addAll(menuNames);
        mMenuNames.trimToSize();

        calViewHeight();

        firstCal = true;
        invalidate();
    }
}