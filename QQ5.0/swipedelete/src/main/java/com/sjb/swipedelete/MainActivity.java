package com.sjb.swipedelete;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {
	private ListView listview;
	private ArrayList<String> list = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listview = (ListView) findViewById(R.id.listview);
		//1.准备数据
		for (int i = 0; i < 30; i++) {
			list.add("name - "+i);
		}
		listview.setAdapter(new MyAdapter());
		
		
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState==OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					//如果垂直滑动，则需要关闭已经打开的layout
					SwipeLayoutManager.getInstance().closeCurrentLayout();
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}



	class MyAdapter extends BaseAdapter implements SwipeLayout.OnSwipeStateChangeListener {
		@Override
		public int getCount() {
			return list.size();
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView=View.inflate(MainActivity.this, R.layout.adapter_list, null);
			}
			ViewHolder holder = ViewHolder.getHolder(convertView);
			holder.tv_delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//删除当前条目数据
					System.out.println("position:"+position);
					Toast.makeText(MainActivity.this,list.get(position)+"删除了", Toast.LENGTH_SHORT).show();
					list.remove(position);
					//关闭
					SwipeLayoutManager.getInstance().closeCurrentLayout();
					//刷新页面
					notifyDataSetChanged();
				}
			});
			holder.tv_name.setText(list.get(position));
			
			holder.swipeLayout.setTag(position);
			holder.swipeLayout.setOnSwipeStateChangeListener(this);
			
			return convertView;
		}
		@Override
		public void onOpen(Object tag) {
//			Toast.makeText(MainActivity.this,"第"+(Integer)tag+"个打开", Toast.LENGTH_SHORT).show();
		}
		@Override
		public void onClose(Object tag) {
//			Toast.makeText(MainActivity.this,"第"+(Integer)tag+"个关闭", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	static class ViewHolder{
		TextView tv_name,tv_delete;
		SwipeLayout swipeLayout;
		public ViewHolder(View convertView){
			tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			tv_delete = (TextView) convertView.findViewById(R.id.tv_delete);
			swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipeLayout);
		}
		public static ViewHolder getHolder(View convertView){
			ViewHolder holder = (ViewHolder) convertView.getTag();
			if(holder==null){
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			return holder;
		}
	}
}
