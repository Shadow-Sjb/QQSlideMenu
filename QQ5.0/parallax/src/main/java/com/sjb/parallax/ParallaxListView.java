package com.sjb.parallax;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ListView;

import com.nineoldandroids.animation.ValueAnimator;

public class ParallaxListView extends ListView {

	public ParallaxListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ParallaxListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ParallaxListView(Context context) {
		super(context);
	}

	private int maxHeight;
	private ImageView imageView;
	private int orignalHeight;

	public void setParallaxImageView( final ImageView imageView){
		this.imageView = imageView;
		//设定最大高度
		imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				//ImageView最初的高度
				orignalHeight = imageView.getHeight();
				//图片的高度
				int drawableHeight = imageView.getDrawable().getIntrinsicHeight();
				//如果控件高度大于图片高度，就将拉动最大高度设为控件高度两倍，
				//如果控件高度小于图片高度，就将拉动最大高度设为图片高度，
				maxHeight = orignalHeight>drawableHeight?
						orignalHeight*2:drawableHeight;
			}
		});
	}


	/**
	 * 在listview滑动到头的时候执行，可以获取到继续滑动的距离和方向
	 * deltaX：继续滑动x方向的距离
	 * deltaY：继续滑动y方向的距离     负：表示顶部到头   正：表示底部到头
	 * maxOverScrollX:x方向最大可以滚动的距离
	 * maxOverScrollY：y方向最大可以滚动的距离
	 * isTouchEvent: true: 是手指拖动滑动     false:表示fling靠惯性滑动;
	 */
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		if(deltaY<0 && isTouchEvent){
			//表示顶部到头，并且是手动拖动到头的情况
			//我们需要不断的增加ImageView的高度
			if(imageView!=null){
				//时差效果
				int newHeight = imageView.getHeight()-deltaY/3;
				//限定最大高度
				if(newHeight>maxHeight)newHeight = maxHeight;
				//重新设置控件高度参数
				imageView.getLayoutParams().height = newHeight;
				imageView.requestLayout();//使ImageView的布局参数生效
			}
		}
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
				scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(ev.getAction()==MotionEvent.ACTION_UP){
			//需要将ImageView的高度缓慢恢复到最初高度
			ValueAnimator animator = ValueAnimator.ofInt(imageView.getHeight(),orignalHeight);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					//获取动画的值，设置给imageview
					int animatedValue = (Integer) animator.getAnimatedValue();
					
					imageView.getLayoutParams().height = animatedValue;
					imageView.requestLayout();//使ImageView的布局参数生效
				}
			});
			animator.setInterpolator(new OvershootInterpolator());//弹性的插值器
			animator.setDuration(350);
			animator.start();
		}
		return super.onTouchEvent(ev);
	}
}
