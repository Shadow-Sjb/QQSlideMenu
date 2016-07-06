package com.sjb.tencent.tencentqq.reminder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.sjb.tencent.tencentqq.util.GeometryUtil;
import com.sjb.tencent.tencentqq.util.Utils;

/**
 * This view should be added to WindowManager, so we can drag it to anywhere.
 * @author PoplarTang
 *
 */
public class GooView extends View {
	
	interface OnDisappearListener {
		void onDisappear(PointF mDragCenter);//消失回调
		void onReset(boolean isOutOfRange);//重置回调
	}

	protected static final String TAG = "TAG";
	
	private PointF mInitCenter;//初始圆心
	private PointF mDragCenter;//拖拽圆圆心
	private PointF mStickCenter;//固定圆圆心
	//圆点半径
	float dragCircleRadius = 0;
	float stickCircleRadius = 0;
	float stickCircleMinRadius = 0;//固定圆最小半径
	float stickCircleTempRadius = stickCircleRadius;
	float farest = 0;//最大拖拽距离
	String text = "";

	private Paint mPaintRed;//红点画笔
	private Paint mTextPaint;//文字画笔
	private ValueAnimator mAnim;//动画
	private boolean isOutOfRange = false;//是否超出范围，默认为false
	private boolean isDisappear = false;//是否消失，默认为消失
	
	private OnDisappearListener mListener;//监听接口
	private Rect rect;//矩形

	private int mStatusBarHeight;//状态栏高度

	private float resetDistance;//重置距离
	
	
	public GooView(Context context) {
		super(context);
		rect = new Rect(0, 0, 50, 50);
		
		stickCircleRadius = Utils.dip2Dimension(10.0f, context);//固定圆半径为10px
		dragCircleRadius = Utils.dip2Dimension(10.0f, context);//拖拽圆半径为10px
		stickCircleMinRadius = Utils.dip2Dimension(3.0f, context);//固定圆最小半径为3px
		farest = Utils.dip2Dimension(80.0f, context);//最远距离为80px
		resetDistance = Utils.dip2Dimension(40.0f, getContext());
		
		mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿
		mPaintRed.setColor(Color.RED);//画笔颜色为红色
		
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿
		mTextPaint.setTextAlign(Align.CENTER);//居中画
		mTextPaint.setColor(Color.WHITE);//白色
		mTextPaint.setTextSize(dragCircleRadius * 1.2f);//字体大小为拖拽源半径的1.2倍
	}

	/**
	 * 设置固定圆的半径
	 * @param r
	 */
	public void setDargCircleRadius(float r){
		dragCircleRadius = r;
	}
	
	/**
	 * 设置拖拽圆的半径
	 * @param r
	 */
	public void setStickCircleRadius(float r){
		stickCircleRadius = r;
	}
	
	/**
	 * 设置数字
	 * @param num
	 */
	public void setNumber(int num){
		text = String.valueOf(num);
	}
	
	/**
	 * 初始化圆的圆心坐标
	 * @param x
	 * @param y
	 */
	public void initCenter(float x, float y){
		mDragCenter = new PointF(x, y);
		mStickCenter = new PointF(x, y);
		mInitCenter = new PointF(x, y);
		invalidate();
	}
	
	/**
	 * 更新拖拽圆的圆心坐标，重绘View
	 * @param x
	 * @param y
	 */
	private void updateDragCenter(float x, float y) {
		this.mDragCenter.x = x;
		this.mDragCenter.y = y;
		invalidate();
	}
	
	/**
	 * 通过绘制Path构建一个ShapeDrawable，用来绘制到画布Canvas上
	 * @return
	 */
	private ShapeDrawable drawGooView() {
		Path path = new Path();

		//1. 根据当前两圆圆心的距离计算出固定圆的半径
		float distance = (float) GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
		stickCircleTempRadius = getCurrentRadius(distance);
		
		//2. 计算出经过两圆圆心连线的垂线的dragLineK（对边比临边）。求出四个交点坐标
		float xDiff = mStickCenter.x - mDragCenter.x;
		Double dragLineK = null;
		if(xDiff != 0){
			dragLineK = (double) ((mStickCenter.y - mDragCenter.y) / xDiff);
		}

		//分别获得经过两圆圆心连线的垂线与圆的交点（两条垂线平行，所以dragLineK相等）。
		PointF[] dragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, dragCircleRadius, dragLineK);
		PointF[] stickPoints = GeometryUtil.getIntersectionPoints(mStickCenter, stickCircleTempRadius, dragLineK);
		
		//3. 以两圆连线的0.618处作为 贝塞尔曲线 的控制点。（选一个中间点附近的控制点）
		PointF pointByPercent = GeometryUtil.getPointByPercent(mDragCenter, mStickCenter, 0.618f);

		// 绘制两圆连接
		//此处参见示意图{@link https://github.com/PoplarTang/DragGooView }
		path.moveTo((float)stickPoints[0].x, (float)stickPoints[0].y);
		path.quadTo((float)pointByPercent.x, (float)pointByPercent.y,
				(float)dragPoints[0].x, (float)dragPoints[0].y);
		path.lineTo((float)dragPoints[1].x, (float)dragPoints[1].y);
		path.quadTo((float)pointByPercent.x, (float)pointByPercent.y,
				(float)stickPoints[1].x, (float)stickPoints[1].y);
		path.close();
		

		//构建ShapeDrawable
		ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(path, 50f, 50f));
		shapeDrawable.getPaint().setColor(Color.RED);
		return shapeDrawable;
	}
	
	/**
	 * 根据距离获得当前固定圆的半径
	 * @param distance
	 * @return
	 */
	private float getCurrentRadius(float distance) {
		
		distance = Math.min(distance, farest);
		
		// Start from 20%
		float fraction = 0.2f + 0.8f * distance / farest;
		
		// Distance -> Farthest
		// stickCircleRadius -> stickCircleMinRadius
		float evaluateValue = (float) GeometryUtil.evaluateValue(fraction, stickCircleRadius, stickCircleMinRadius);
		return evaluateValue;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(isAnimRunning()){
			return false;
		}
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int actionMasked = MotionEventCompat.getActionMasked(event);
		switch (actionMasked) {
			case MotionEvent.ACTION_DOWN:{
				if(isAnimRunning()){
					return false;
				}
				isDisappear = false;
				isOutOfRange = false;
				updateDragCenter(event.getRawX() , event.getRawY());
				break;
			}
			case MotionEvent.ACTION_MOVE:{
				//如果两圆间距大于最大距离farest，执行拖拽结束动画
				PointF p0 = new PointF(mDragCenter.x, mDragCenter.y);
				PointF p1 = new PointF(mStickCenter.x, mStickCenter.y);
				if (GeometryUtil.getDistanceBetween2Points(p0, p1) > farest) {
					isOutOfRange = true;
					updateDragCenter(event.getRawX(), event.getRawY());
					return false;
				}
				
				updateDragCenter(event.getRawX() , event.getRawY());
				break;
			}
			case MotionEvent.ACTION_UP:{
				handleActionUp();
				break;
			}
			default:{
				isOutOfRange = false;
				break;
			}
		}
		return true;
	}

	/**
	 * 动画是否在执行
	 * @return
     */
	private boolean isAnimRunning() {
		if(mAnim != null && mAnim.isRunning()){//动画不等于空且正在执行
			return true;
		}
		return false;
	}

	/**
	 * 清除
	 */
	private void disappeared() {
		isDisappear = true;
		invalidate();//重绘
		
		if(mListener != null){
			mListener.onDisappear(mDragCenter);//监听回调
		}
	}


	private void handleActionUp() {
		if(isOutOfRange){//如果超出范围了
			// When user drag it back, we should call onReset().
			if(GeometryUtil.getDistanceBetween2Points(mDragCenter, mInitCenter) < resetDistance){
				//如果没有超过重置范围，则重置
				if(mListener != null)
					mListener.onReset(isOutOfRange);//重置
				return;
			}
			// Otherwise
			disappeared();//消失
		}else {
			//没有超过范围
			//手指抬起时，弹回动画
			mAnim = ValueAnimator.ofFloat(1.0f);
			mAnim.setInterpolator(new OvershootInterpolator(4.0f));//反弹插补器

			final PointF startPoint = new PointF(mDragCenter.x, mDragCenter.y);
			final PointF endPoint = new PointF(mStickCenter.x, mStickCenter.y);
			mAnim.addUpdateListener(new AnimatorUpdateListener() {//动画更新监听
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float fraction = animation.getAnimatedFraction();//获取百分比
					PointF pointByPercent = GeometryUtil.getPointByPercent(startPoint, endPoint, fraction);
					updateDragCenter((float)pointByPercent.x, (float)pointByPercent.y);
				}
			});
			mAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if(mListener != null)
						mListener.onReset(isOutOfRange);
				}
			});

			if(GeometryUtil.getDistanceBetween2Points(startPoint,endPoint) < 10)	{
				mAnim.setDuration(10);
			}else {
				mAnim.setDuration(500);
			}
			mAnim.start();//开启动画
		}
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		//去除状态栏高度偏差
		canvas.translate(0, -mStatusBarHeight);
		
		if(!isDisappear){//如果没有消失
			if(!isOutOfRange){//没有超出范围
				// 画两圆连接处
				ShapeDrawable drawGooView = drawGooView();
				drawGooView.setBounds(rect);
				drawGooView.draw(canvas);
				// 画固定圆
				canvas.drawCircle(mStickCenter.x, mStickCenter.y, stickCircleTempRadius, mPaintRed);
			}
			// 画拖拽圆
			canvas.drawCircle(mDragCenter.x , mDragCenter.y, dragCircleRadius, mPaintRed);
			// 画数字
			canvas.drawText(text, mDragCenter.x , mDragCenter.y + dragCircleRadius /2f, mTextPaint);
		}
		canvas.restore();
	}

	public OnDisappearListener getOnDisappearListener() {
		return mListener;
	}

	public void setOnDisappearListener(OnDisappearListener mListener) {
		this.mListener = mListener;
	}

	public void setStatusBarHeight(int statusBarHeight) {
		this.mStatusBarHeight = statusBarHeight;
	}

}
