package com.sjb.tencent.tencentqq.drag;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


public class DragRelativeLayout extends RelativeLayout {

    private DragLayout dl;

	public DragRelativeLayout(Context context) {
        super(context);
    }

    public DragRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * 设置滑动页面
     * @param dl
     */
    public void setDragLayout(DragLayout dl) {
        this.dl = dl;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (dl.getStatus() != DragLayout.Status.Close) {//当前侧滑面板打开
            return true;//消费事件
        }
        return super.onInterceptTouchEvent(event);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (dl.getStatus() != DragLayout.Status.Close) {//当前侧滑面板打开
			if (event.getAction() == MotionEvent.ACTION_UP) {//抬起
				dl.close(true);//关闭
			}
			return true;//消费事件
		}
		return super.onTouchEvent(event);
	}
}
