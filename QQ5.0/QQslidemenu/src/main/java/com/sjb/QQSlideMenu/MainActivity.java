package com.sjb.QQSlideMenu;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Random;

public class MainActivity extends Activity {
    private ListView menu_listview, main_listview;
    private SlideMenu slideMenu;
    private ImageView iv_head;
    private MyLinearLayout my_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }


    /**
     * 初始化数据
     */
    private void initData() {
        //初始化菜单listview数据
        menu_listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });
        //初始化主页面listview数据
        main_listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.NAMES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView == null ? super.getView(position, convertView, parent) : convertView;
                //先缩小view
                ViewHelper.setScaleX(view, 0.5f);
                ViewHelper.setScaleY(view, 0.5f);
                //以属性动画放大
                ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
                ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();
                return view;
            }
        });

        //设置滑动菜单
        my_layout.setSlideMenu(slideMenu);

        //设置slidemenu滑动监听
        slideMenu.setOnDragStateChangeListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                //菜单listview随机选中条目
                menu_listview.smoothScrollToPosition(new Random().nextInt(menu_listview.getCount()));
            }

            @Override
            public void onClose() {
                //菜单关闭后，主页面左上角图标抖动动画
                ViewPropertyAnimator.animate(iv_head).translationXBy(15)
                        .setInterpolator(new CycleInterpolator(4))
                        .setDuration(500)
                        .start();
            }

            @Override
            public void onDraging(float fraction) {
                //主页面左上角图标透明动画
                ViewHelper.setAlpha(iv_head, 1 - fraction);
            }
        });

    }

    /**
     * 初始化控件
     */
    private void initView() {
        menu_listview = (ListView) findViewById(R.id.menu_listview);
        main_listview = (ListView) findViewById(R.id.main_listview);
        slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        my_layout = (MyLinearLayout) findViewById(R.id.my_layout);
    }

}
