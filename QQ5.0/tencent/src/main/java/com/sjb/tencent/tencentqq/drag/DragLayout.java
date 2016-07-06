package com.sjb.tencent.tencentqq.drag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;
import com.sjb.tencent.tencentqq.swipe.SwipeListAdapter;

/**
 * 侧滑面板
 */
public class DragLayout extends FrameLayout {

    private View mLeftContent;//左侧菜单
    private View mMainContent;//主页面
    private View mRightContent;//右侧菜单
    private int mWidth;//主菜单宽度
    private int mHeight;//主菜单高度
    private int mRangeLeft;//移动的最大范围
    private int mRightWidth;//右侧菜单宽度
    private int mRangeRight;//移动右侧最大距离
    private ViewDragHelper mDragHelper;//ViewDragHelper对象
    private GestureDetectorCompat mGestureDetector;//手势指示器

    private Status mStatus = Status.Close;//当前状态，默认为关闭
    private Direction mDirction = Direction.Left;//初始化时方向为左
    private OnDragListener mDragListener;
    private boolean mScaleEnable = true;//默认侧滑模式

    //回调接口
    public interface OnDragListener {
        //回调方法
        void onOpen();//侧滑面板打开

        void onClose();//侧滑面板关闭

        void onDrag(float percent);//侧滑面板滑动过程中

        void onStartOpen(Direction direction);//侧滑面板打开，回传当时打开方向
    }

    //枚举，记录当前状态
    public enum Status {
        Open, Close, Draging
    }

    //枚举，记录当前打开方向
    public enum Direction {
        Left, Right, defualt
    }

    public DragLayout(Context context) {
        this(context, null);
        init(context);
    }


    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        //创建ViewDragHelper对象，来处理触摸事件
        mDragHelper = ViewDragHelper.create(this, mCallBack);
        mGestureDetector = new GestureDetectorCompat(context, mYGestureListener);//手势指示器
    }


    SimpleOnGestureListener mYGestureListener = new SimpleOnGestureListener() {
        /**
         * @param e1 The first down motion event that started the scrolling.
         @param e2 The move motion event that triggered the current onScroll.
         @param distanceX The distance along the X axis(轴) that has been scrolled since the last call to onScroll. This is NOT the distance between e1 and e2.
         @param distanceY The distance along the Y axis that has been scrolled since the last call to onScroll. This is NOT the distance between e1 and e2.
         无论是用手拖动view，或者是以抛的动作滚动，都会多次触发 ,这个方法在ACTION_MOVE动作发生时就会触发 参看GestureDetector的onTouchEvent方法源码
          * */
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return Math.abs(distanceX) >= Math.abs(distanceY);//如果是上下滑动返回true，否则返回false
        }
    };

    //读取完xml文件后调用，用来初始化子view
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftContent = (View) getChildAt(0);//左侧菜单
        mRightContent = getChildAt(1);//右侧菜单
        mMainContent = (View) getChildAt(2);//主页

    }

    //初始化子控件宽高
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //主页面宽高
        mWidth = mMainContent.getMeasuredWidth();
        mHeight = mMainContent.getMeasuredHeight();
        //右菜单宽度
        mRightWidth = mRightContent.getMeasuredWidth();
        //移动的范围
        mRangeLeft = (int) (mWidth * 0.6f);
        mRangeRight = mRightWidth;//移动的右侧最大距离
    }

    float mDownX;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getStatus() == Status.Close) {//如果当前状态为关闭
            int actionMasked = MotionEventCompat.getActionMasked(ev);
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = ev.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (adapter.getUnClosedCount() > 0) {//当前有未关闭的条目
                        return false;
                    }
                    float delta = ev.getRawX() - mDownX;
                    if (delta < 0) {
                        return false;
                    }
                    break;
                default:
                    mDownX = 0;
                    break;
            }
        }
        return mDragHelper.shouldInterceptTouchEvent(ev)
                & mGestureDetector.onTouchEvent(ev);
    }

    //把触摸事件交由viewdraghelper处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    private int mMainLeft = 0;//主菜单的左边距，开始时为0

    ViewDragHelper.Callback mCallBack = new ViewDragHelper.Callback() {

        //捕捉子控件的触摸事件
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;//所有子控件都捕获
        }

        //水平移动的范围
        @Override
        public int getViewHorizontalDragRange(View child) {
            // 2. 决定拖拽的范围
            return mWidth;
        }

        //启用移动
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // 3. 决定拖动时的位置，可在这里进行位置修正。（若想在此方向拖动，必须重写，因为默认返回0）
            //dx 本次移动的距离
            //left 当前控件的左边距
            return clampResult(mMainLeft + dx, left);
        }

        //用来做子空间之间的伴随移动
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            // 4. 决定了当View被拖动时，希望同时引发的其他变化
            if (changedView == mMainContent) {//拖动的是主菜单
                mMainLeft = left;
            } else {
                mMainLeft += dx;
            }
            mMainLeft = clampResult(mMainLeft, mMainLeft);//限制边界

            if (changedView == mLeftContent || changedView == mRightContent) {//当拖动的是左侧菜单或者右侧菜单
                layoutContent();
            }
            dispathDragEvent(mMainLeft);
            invalidate();//重画页面
        }

        /**
         * @param releasedChild
         *            被释放的孩子
         * @param xvel
         *            释放时X方向的速度
         * @param yvel
         *            释放时Y方向的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // 5. 决定当childView被释放时，希望做的事情——执行打开/关闭动画，更新状态
            boolean scrollRight = xvel > 1.0f;
            boolean scrollLeft = xvel < -1.0f;
            if (scrollRight || scrollLeft) {
                if (scrollRight && mDirction == Direction.Left) {
                    open(true, mDirction);
                } else if (scrollLeft && mDirction == Direction.Right) {
                    open(true, mDirction);
                } else {
                    close(true);
                }
                return;
            }

            if (releasedChild == mLeftContent && mMainLeft > mRangeLeft * 0.7f) {
                open(true, mDirction);
            } else if (releasedChild == mMainContent) {
                if (mMainLeft > mRangeLeft * 0.3f)
                    open(true, mDirction);
                else if (-mMainLeft > mRangeRight * 0.3f)
                    open(true, mDirction);
                else
                    close(true);
            } else if (releasedChild == mRightContent
                    && -mMainLeft > mRangeRight * 0.7f) {
                open(true, mDirction);
            } else {
                close(true);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (mStatus == Status.Close && state == ViewDragHelper.STATE_IDLE
                    && mDirction == Direction.Right) {
                mDirction = Direction.Left;
            }
        }

    };

    //计算移动距离，限制边界
    private int clampResult(int tempValue, int defaultValue) {
        Integer minLeft = null;//最小左边距
        Integer maxLeft = null;//最大左边距

        if (mDirction == Direction.Left) {//打开左菜单
            //移动范围为0 -- 左距离
            minLeft = 0;
            maxLeft = 0 + mRangeLeft;
        } else if (mDirction == Direction.Right) {//打开右菜单
            //移动范围为 -右侧菜单宽度 -- 0
            minLeft = 0 - mRangeRight;
            maxLeft = 0;
        }

        if (minLeft != null && tempValue < minLeft)//当前偏移超出左边界
            return minLeft;//限定左边界
        else if (maxLeft != null && tempValue > maxLeft)//当前偏移超出右边界
            return maxLeft;//限定右边界
        else
            return defaultValue;//未超出边界，返回当前值
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutContent();
    }

    /**
     * 摆放子控件，并根据mainleft的值移动主页面的位置
     */
    private void layoutContent() {
        mLeftContent.layout(0, 0, mWidth, mHeight);
        mRightContent.layout(mWidth - mRightWidth, 0, mWidth, mHeight);
        mMainContent.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setDragListener(OnDragListener mDragListener) {
        this.mDragListener = mDragListener;
    }

    /**
     * 处理其他同步动画
     *
     * @param mainLeft
     */
    protected void dispathDragEvent(int mainLeft) {

        // 注意转换成float
        float percent = 0;//移动百分比
        if (mDirction == Direction.Left)//打开左菜单
            percent = mainLeft / (float) mRangeLeft;
        else if (mDirction == Direction.Right)//移动右菜单
            percent = Math.abs(mainLeft) / (float) mRangeRight;

        if (mDragListener != null) {
            mDragListener.onDrag(percent);//回调接口
        }

        // 更新动画
        if (mScaleEnable) {
            animViews(percent);//根据拖动百分比更新动画
        }
        // 更新状态
        Status lastStatus = mStatus;//记录状态
        if (updateStatus() != lastStatus) {//状态改变了
            if (lastStatus == Status.Close && mStatus == Status.Draging) {//原来状态是关闭，现在在拖动
                //如果正在打开左测菜单则显示它，否则隐藏
                mLeftContent.setVisibility(mDirction == Direction.Left ? View.VISIBLE : View.GONE);
                //如果正在打开右测滑面板，则显示右测菜单，否则隐藏
                mRightContent.setVisibility(mDirction == Direction.Right ? View.VISIBLE : View.GONE);
                if (mDragListener != null) {
                    mDragListener.onStartOpen(mDirction);//开始打开侧滑菜单回调接口，并传回打开的方向
                }
            }

            if (mStatus == Status.Close) {//当前状态为关闭
                if (mDragListener != null)
                    mDragListener.onClose();//回调接口，关闭菜单
            } else if (mStatus == Status.Open) {
                if (mDragListener != null)
                    mDragListener.onOpen();//回调接口，打开菜单
            }
        }
    }

    /**
     * 更新状态
     *
     * @return
     */
    private Status updateStatus() {
        if (mDirction == Direction.Left) {//左
            if (mMainLeft == 0) {
                mStatus = Status.Close;//左侧滑面板关闭
            } else if (mMainLeft == mRangeLeft) {
                mStatus = Status.Open;//左侧滑面板打开
            } else {
                mStatus = Status.Draging;//正在拖动
            }
        } else if (mDirction == Direction.Right) {//右
            if (mMainLeft == 0) {
                mStatus = Status.Close;//右侧滑面板关闭
            } else if (mMainLeft == 0 - mRangeRight) {
                mStatus = Status.Open;//右侧滑面板打开
            } else {
                mStatus = Status.Draging;//正在拖动
            }
        }
        return mStatus;
    }

    /**
     * 动画
     *
     * @param percent
     */
    private void animViews(float percent) {
        animMainView(percent);//主页面动画
        animBackView(percent);//打开菜单动画
    }

    /**
     * 侧滑面板动画
     * @param percent
     */
    private void animBackView(float percent) {
        if (mDirction == Direction.Right) {//右侧菜单打开
            // 右边栏X, Y放大，向左移动, 逐渐显示
            ViewHelper.setScaleX(mRightContent, 0.5f + 0.5f * percent);
            ViewHelper.setScaleY(mRightContent, 0.5f + 0.5f * percent);
            ViewHelper.setTranslationX(mRightContent, evaluate(percent, mRightWidth + mRightWidth / 2.0f, 0.0f));
            ViewHelper.setAlpha(mRightContent, percent);
        } else {
            //左侧菜单打开
            // 左边栏X, Y放大，向右移动, 逐渐显示
            ViewHelper.setScaleX(mLeftContent, 0.5f + 0.5f * percent);
            ViewHelper.setScaleY(mLeftContent, 0.5f + 0.5f * percent);
            ViewHelper.setTranslationX(mLeftContent, evaluate(percent, -mWidth / 2f, 0.0f));
            ViewHelper.setAlpha(mLeftContent, percent);
        }
        // 背景逐渐变亮
        getBackground().setColorFilter(caculateValue(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    /**
     * 主页面动画
     * @param percent
     */
    private void animMainView(float percent) {
        Float inverseP = null;
        if (mDirction == Direction.Left) {//打开左测菜单
            inverseP = 1 - percent * 0.25f;
        } else if (mDirction == Direction.Right) {//打开右侧菜单
            inverseP = 1 - percent * 0.25f;
        }
        // 主界面X,Y缩小
        if (inverseP != null) {
            if (mDirction == Direction.Right) {
                ViewHelper.setPivotX(mMainContent, mWidth);
                ViewHelper.setPivotY(mMainContent, mHeight / 2.0f);
            } else {
                ViewHelper.setPivotX(mMainContent, mWidth / 2.0f);
                ViewHelper.setPivotY(mMainContent, mHeight / 2.0f);
            }
            ViewHelper.setScaleX(mMainContent, inverseP);
            ViewHelper.setScaleY(mMainContent, inverseP);
        }
    }


    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * colorUtils 计算两种color的过度
     * @param fraction
     * @param start
     * @param end
     * @return
     */
    private int caculateValue(float fraction, Object start, Object end) {

        int startInt = (Integer) start;
        int startIntA = startInt >> 24 & 0xff;
        int startIntR = startInt >> 16 & 0xff;
        int startIntG = startInt >> 8 & 0xff;
        int startIntB = startInt & 0xff;

        int endInt = (Integer) end;
        int endIntA = endInt >> 24 & 0xff;
        int endIntR = endInt >> 16 & 0xff;
        int endIntG = endInt >> 8 & 0xff;
        int endIntB = endInt & 0xff;

        return ((int) (startIntA + (endIntA - startIntA) * fraction)) << 24
                | ((int) (startIntR + (endIntR - startIntR) * fraction)) << 16
                | ((int) (startIntG + (endIntG - startIntG) * fraction)) << 8
                | ((int) (startIntB + (endIntB - startIntB) * fraction));
    }

    /**
     * 关闭侧滑菜单
     */
    public void close() {
        close(true);
    }

    /**
     * 关闭侧滑菜单
     */
    public void close(boolean withAnim) {
        mMainLeft = 0;
        if (withAnim) {
            if (mDragHelper.smoothSlideViewTo(mMainContent, mMainLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent();
            dispathDragEvent(mMainLeft);
        }
    }

    /**
     * 打开
     */
    public void open() {
        open(true);
    }

    /**
     * 打开，是否启用动画
     */
    public void open(boolean withAnim) {
        open(withAnim, Direction.Left);
    }

    /**
     * 打开，是否启用动画，打开的方向
     */
    public void open(boolean withAnim, Direction d) {
        mDirction = d;
        if (mDirction == Direction.Left)//方向为左
            mMainLeft = mRangeLeft;
        else if (mDirction == Direction.Right)//方向为右
            mMainLeft = -mRangeRight;

        if (withAnim) {
            // 引发动画的开始
            if (mDragHelper.smoothSlideViewTo(mMainContent, mMainLeft, 0)) {//平滑的打开左侧菜单或右侧菜单
                // 需要在computeScroll中使用continueSettling方法才能将动画继续下去（因为ViewDragHelper使用了scroller）。
                ViewCompat.postInvalidateOnAnimation(this);//刷新页面
            }
        } else {
            layoutContent();
            dispathDragEvent(mMainLeft);
        }
    }


    /**
     * 获取当前状态
     *
     * @return
     */
    public Status getStatus() {
        return mStatus;
    }

    //切花滑动效果
    public void switchScaleEnable() {
        this.mScaleEnable = !mScaleEnable;
        if (!mScaleEnable) {
            animBackView(1.0f);
        }

    }

    private SwipeListAdapter adapter;
    /**
     * 设置适配器
     * @param adapter
     */
    public void setAdapterInterface(SwipeListAdapter adapter) {
        this.adapter = adapter;
    }

}
