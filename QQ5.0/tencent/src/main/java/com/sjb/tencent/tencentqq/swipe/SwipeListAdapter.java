package com.sjb.tencent.tencentqq.swipe;

import android.content.Context;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjb.tencent.R;
import com.sjb.tencent.tencentqq.domain.Cheeses;
import com.sjb.tencent.tencentqq.reminder.GooViewListener;
import com.sjb.tencent.tencentqq.util.Utils;

import java.util.HashSet;


public class SwipeListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();//打开的swipelayout集合

	public SwipeListAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return 120;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	//头像数组
	public static final int[] HEAD_IDS = new int[]{
		R.drawable.head_1,
		R.drawable.head_2,
		R.drawable.head_3
	};
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder;
		if (convertView != null) {
			mHolder = (ViewHolder) convertView.getTag();
		}else {
			//加载布局
			convertView = mInflater.inflate(R.layout.list_item_swipe, null);
			mHolder = ViewHolder.fromValues(convertView);
			convertView.setTag(mHolder);//绑定holder
		}
		SwipeLayout view = (SwipeLayout) convertView;
		view.close(false, false);
		view.getFrontView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showToast(mContext, "item click: " + position);
			}
		});

		view.setSwipeListener(mSwipeListener);//设置滑动监听

		//初始化数据
		mHolder.mImage.setImageResource(HEAD_IDS[position % HEAD_IDS.length]);
		mHolder.mName.setText(Cheeses.NAMES[position % Cheeses.NAMES.length]);

		//call按钮设置标签和点击事件
		mHolder.mButtonCall.setTag(position);
		mHolder.mButtonCall.setOnClickListener(onActionClick);
		//delete按钮设置标签和点击事件
		mHolder.mButtonDel.setTag(position);
		mHolder.mButtonDel.setOnClickListener(onActionClick);
		
		//未读消息气泡
		TextView mUnreadView = mHolder.mReminder;
		boolean visiable = !mRemoved.contains(position);//根据是否移出来设置控件的显示与隐藏
		mUnreadView.setVisibility(visiable ? View.VISIBLE : View.GONE);

		if (visiable) {
			mUnreadView.setText(String.valueOf(position));
			mUnreadView.setTag(position);
			GooViewListener mGooListener = new GooViewListener(mContext, mUnreadView) {
				@Override
				public void onDisappear(PointF mDragCenter) {
					super.onDisappear(mDragCenter);

					mRemoved.add(position);
					notifyDataSetChanged();
					Utils.showToast(mContext,
							"Cheers! We have get rid of it!");
				}
				@Override
				public void onReset(boolean isOutOfRange) {
					super.onReset(isOutOfRange);
					notifyDataSetChanged();
					Utils.showToast(mContext,
							isOutOfRange ? "Are you regret?" : "Try again!");
				}
			};
			mUnreadView.setOnTouchListener(mGooListener);
		}
		return view;
	}

	OnClickListener onActionClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Integer p = (Integer) v.getTag();
			int id = v.getId();
			if (id == R.id.bt_call) {
				closeAllLayout();//关闭所有打开的swipelayout
				Utils.showToast(mContext, "position: " + p + " call");
			} else if (id == R.id.bt_delete) {
				closeAllLayout();//关闭所有打开的swipelayout
				Utils.showToast(mContext, "position: " + p + " del");
			}
		}
	};

	SwipeLayout.SwipeListener mSwipeListener = new SwipeLayout.SwipeListener() {
		@Override
		public void onOpen(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onOpen");
			mUnClosedLayouts.add(swipeLayout);
		}

		@Override
		public void onClose(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onClose");
			mUnClosedLayouts.remove(swipeLayout);//swipelayout关闭，从集合中移除
		}

		@Override
		public void onStartClose(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onStartClose");
		}

		@Override
		public void onStartOpen(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onStartOpen");
			closeAllLayout();
			mUnClosedLayouts.add(swipeLayout);
		}

	};
	public int getUnClosedCount(){
		return mUnClosedLayouts.size();
	}

	//关闭swipelayout
	public void closeAllLayout() {
		if(mUnClosedLayouts.size() == 0)//当前没有打开的，直接返回
			return;
		for (SwipeLayout l : mUnClosedLayouts) {//遍历集合，全部关闭
			l.close(true, false);
		}
		mUnClosedLayouts.clear();//清空集合
	}
	
	static class ViewHolder {
		public ImageView mImage;
		public Button mButtonCall;
		public Button mButtonDel;
		public TextView mReminder;
		public TextView mName;
		
		private ViewHolder(ImageView mImage, Button mButtonCall,
				Button mButtonDel, TextView mReminder, TextView mName) {
			super();
			this.mImage = mImage;
			this.mButtonCall = mButtonCall;
			this.mButtonDel = mButtonDel;
			this.mReminder = mReminder;
			this.mName = mName;
		}

		/**
		 * 初始化view中控件，并返回Viewholder对象
		 * @param view
         * @return
         */
		public static ViewHolder fromValues(View view) {
			return new ViewHolder(
				(ImageView) view.findViewById(R.id.iv_head),
				(Button) view.findViewById(R.id.bt_call),
				(Button) view.findViewById(R.id.bt_delete),
				(TextView) view.findViewById(R.id.point),
				(TextView) view.findViewById(R.id.tv_name));
		}
	}

}