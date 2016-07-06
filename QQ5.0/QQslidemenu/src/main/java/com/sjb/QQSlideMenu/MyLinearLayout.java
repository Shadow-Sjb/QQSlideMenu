package com.sjb.QQSlideMenu;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 当slideMenu打开的时候，拦截并消费掉触摸事件
 *
 */
public class MyLinearLayout extends LinearLayout {
    private boolean isClick = true;
    private float mStartX;
    private float mStartY;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private SlideMenu slideMenu;

    public void setSlideMenu(SlideMenu slideMenu) {
        this.slideMenu = slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (slideMenu != null && slideMenu.getCurrentState() == SlideMenu.DragState.Open) {
            //如果slideMenu打开则应该拦截并消费掉事件
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (slideMenu != null && slideMenu.getCurrentState() == SlideMenu.DragState.Open) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = event.getX();
                    mStartY = event.getY();
                    isClick = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float endX = event.getX();
                    float endY = event.getY();
                    float xOffset = endX - mStartX;
                    float yOffset = endY - mStartY;
                    if (Math.abs(xOffset) < 5 && Math.abs(yOffset) < 5) {
                        isClick = true;
                    }else {
                        isClick = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isClick) {
                        slideMenu.close();
                    }
                    break;
                default:
                    break;
            }
            //如果slideMenu打开则应该拦截并消费掉事件
            return true;
        }
        return super.onTouchEvent(event);
    }
}
