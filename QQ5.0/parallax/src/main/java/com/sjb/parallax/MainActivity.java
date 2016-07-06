package com.sjb.parallax;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author ShenJiaBin
 * @date 2016-7-4.21:38
 */
public class MainActivity extends AppCompatActivity {
    private ParallaxListView listview;
    private String[] indexArr = {"A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview = (ParallaxListView) findViewById(R.id.list_view);

        listview.setOverScrollMode(ListView.OVER_SCROLL_NEVER);//永远不显示蓝色阴影

        //添加header
        View headerView = View.inflate(this, R.layout.layout_header, null);
        ImageView imageView = (ImageView) headerView.findViewById(R.id.imageView);
        listview.setParallaxImageView(imageView);

        listview.addHeaderView(headerView);

        listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, indexArr) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textview = (TextView) super.getView(position, convertView, parent);
                textview.setGravity(Gravity.CENTER_HORIZONTAL);
                return textview;
            }
        });
    }
}
