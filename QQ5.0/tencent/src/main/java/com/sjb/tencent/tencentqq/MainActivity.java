package com.sjb.tencent.tencentqq;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.sjb.tencent.R;
import com.sjb.tencent.tencentqq.domain.Cheeses;
import com.sjb.tencent.tencentqq.drag.DragLayout;
import com.sjb.tencent.tencentqq.drag.DragRelativeLayout;
import com.sjb.tencent.tencentqq.swipe.SwipeListAdapter;
import com.sjb.tencent.tencentqq.util.Utils;

import java.util.Random;

public class MainActivity extends ListActivity implements OnClickListener {

    private DragLayout mDragLayout;//滑动页面
    private ImageView mHeader;
    private ImageView mBtRight;
    private SwipeListAdapter adapter;//滑动删除数据适配器
    private ListView mLeftList;//左侧listview
    private RelativeLayout mLlHeader;
    private TextView mTv_constant;
    private TextView mTv_life1;
    private TextView tv_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载布局
        setContentView(R.layout.activity_main);
        //初始化左测菜单和主页
        initLeftContent();
        initMainContent();
    }

    /**
     * 初始化主页面
     */
    private void initMainContent() {
        //初始化DragLayout
        mDragLayout = (DragLayout) findViewById(R.id.dsl);
        mDragLayout.setDragListener(mDragListener);//设置监听

        DragRelativeLayout mMainView = (DragRelativeLayout) findViewById(R.id.rl_main);//主页面
        mMainView.setDragLayout(mDragLayout);//设置DragLayout
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        mTv_constant = (TextView) findViewById(R.id.tv_constant);
        mTv_life1 = (TextView) findViewById(R.id.tv_life);

        tv_msg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"xiaoxi",Toast.LENGTH_SHORT).show();
            }
        });
        mTv_life1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"dongtai",Toast.LENGTH_SHORT).show();
            }
        });
        mTv_constant.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"lianxiren",Toast.LENGTH_SHORT).show();
            }
        });
        mHeader = (ImageView) findViewById(R.id.iv_head);
        mBtRight = (ImageView) findViewById(R.id.iv_head_right);//标题栏右侧图标
        mLlHeader = (RelativeLayout) findViewById(R.id.ll_header);
        mLlHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mLlHeader.requestDisallowInterceptTouchEvent(true);//自己处理
                return true;//消费事件
            }
        });

        mHeader.setOnClickListener(this);
        mBtRight.setOnClickListener(this);

        mHeader.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //长按点击事件，切换滑动效果
                mDragLayout.switchScaleEnable();
                return true;//消费事件
            }
        });

        //给当前listActivity设置数据
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings));
        adapter = new SwipeListAdapter(MainActivity.this);
        setListAdapter(adapter);//给listview设置swipelayout条目
        mDragLayout.setAdapterInterface(adapter);

        //给listview添加滑动监听
        getListView().setOnScrollListener(new OnScrollListener() {
            //当滑动状态改变时调用
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    adapter.closeAllLayout();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

    }

    /**
     * 初始化左测菜单
     */
    private void initLeftContent() {

        mLeftList = (ListView) findViewById(R.id.lv_left);
        mLeftList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings) {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = (TextView) view.findViewById(android.R.id.text1);
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });
    }

    //创建dragLayout监听
    private DragLayout.OnDragListener mDragListener = new DragLayout.OnDragListener() {
        //菜单打开时
        @Override
        public void onOpen() {
            mLeftList.smoothScrollToPosition(new Random().nextInt(30));
        }
        //菜单关闭
        @Override
        public void onClose() {
            shakeHeader();//抖动头像
            mBtRight.setSelected(false);//未选中
        }

        //滑动过程中调用
        @Override
        public void onDrag(final float percent) {
            // 主界面左上角头像渐渐消失
            ViewHelper.setAlpha(mHeader, 1 - percent);
        }

        //两侧菜单开始打开时调用
        //direction :打开的侧滑面板方向
        @Override
        public void onStartOpen(DragLayout.Direction direction) {
            Utils.showToast(getApplicationContext(), "onStartOpen: " + direction.toString());
        }
    };


    //点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_head://左上角头像
                mDragLayout.open(true);//打开左测菜单
                break;
            case R.id.iv_head_right:
                mDragLayout.open(true, DragLayout.Direction.Right);
                mBtRight.setSelected(true);//打开右侧菜单
                break;
            default:
                break;
        }
    }

    /**
     * 左上角头像抖动动画
     */
    private void shakeHeader() {
        mHeader.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    //返回键事件处理
    @Override
    public void onBackPressed() {
        //如果侧滑面板打开，则关闭侧滑面板
        if (mDragLayout.getStatus() != DragLayout.Status.Close) {
            mDragLayout.close();
            return;
        }
        super.onBackPressed();
    }

}
