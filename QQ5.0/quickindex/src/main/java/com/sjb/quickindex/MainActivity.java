package com.sjb.quickindex;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends Activity {
    private QuickIndexBar quickIndexBar;
    private ListView listview;
    private TextView currentWord;

    private ArrayList<Friend> friends = new ArrayList<Friend>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        quickIndexBar = (QuickIndexBar) findViewById(R.id.quickIndexBar);
        listview = (ListView) findViewById(R.id.listview);
        currentWord = (TextView) findViewById(R.id.currentWord);

        //1.准备数据
        fillList();
        //2.对数据进行排序
        Collections.sort(friends);
        //3.设置Adapter
        listview.setAdapter(new MyAdapter());

        quickIndexBar.setOnTouchLetterListener(new QuickIndexBar.OnTouchLetterListener() {
            @Override
            public void onTouchLetter(String letter) {
                //根据当前触摸的字母，去集合中找那个item的首字母和letter一样，然后将对应的item放到屏幕顶端
                for (int i = 0; i < friends.size(); i++) {
                    String firstWord = friends.get(i).getPinyin().charAt(0) + "";
                    if (letter.equals(firstWord)) {
                        //说明找到了，那么应该讲当前的item放到屏幕顶端
                        listview.setSelection(i);
                        break;//只需要找到第一个就行
                    }
                }
                //显示当前触摸的字母
                showCurrentWord(letter);
            }
        });

        //通过缩小currentWord来隐藏
        ViewHelper.setScaleX(currentWord, 0);
        ViewHelper.setScaleY(currentWord, 0);

    }

    private boolean isScale = false;
    private Handler handler = new Handler();

    protected void showCurrentWord(String letter) {
        currentWord.setText(letter);
        if (!isScale) {
            isScale = true;
            ViewPropertyAnimator.animate(currentWord).scaleX(1f)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(450).start();
            ViewPropertyAnimator.animate(currentWord).scaleY(1f)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(450).start();
        }
        //先移除之前的任务
        handler.removeCallbacksAndMessages(null);
        //延时隐藏currentWord
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewPropertyAnimator.animate(currentWord).scaleX(0f).setDuration(450).start();
                ViewPropertyAnimator.animate(currentWord).scaleY(0f).setDuration(450).start();
                isScale = false;
            }
        }, 1500);
    }

    /**
     * 填充数据
     */
    private void fillList() {
        // 虚拟数据
        friends.add(new Friend("李伟"));
        friends.add(new Friend("张三"));
        friends.add(new Friend("阿三"));
        friends.add(new Friend("阿四"));
        friends.add(new Friend("段誉"));
        friends.add(new Friend("段正淳"));
        friends.add(new Friend("张三丰"));
        friends.add(new Friend("陈坤"));
        friends.add(new Friend("林俊杰1"));
        friends.add(new Friend("陈坤2"));
        friends.add(new Friend("王二a"));
        friends.add(new Friend("林俊杰a"));
        friends.add(new Friend("张四"));
        friends.add(new Friend("林俊杰"));
        friends.add(new Friend("王二"));
        friends.add(new Friend("王二b"));
        friends.add(new Friend("赵四"));
        friends.add(new Friend("杨坤"));
        friends.add(new Friend("赵子龙"));
        friends.add(new Friend("杨坤1"));
        friends.add(new Friend("李伟1"));
        friends.add(new Friend("宋江"));
        friends.add(new Friend("宋江1"));
        friends.add(new Friend("李伟3"));
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friends.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.adapter_friend, null);
            }

            ViewHolder holder = ViewHolder.getHolder(convertView);
            //设置数据
            Friend friend = friends.get(position);
            holder.name.setText(friend.getName());

            String currentWord = friend.getPinyin().charAt(0) + "";
            if (position > 0) {
                //获取上一个item的首字母
                String lastWord = friends.get(position - 1).getPinyin().charAt(0) + "";
                //拿当前的首字母和上一个首字母比较
                if (currentWord.equals(lastWord)) {
                    //说明首字母相同，需要隐藏当前item的first_word
                    holder.first_word.setVisibility(View.GONE);
                } else {
                    //不一样，需要显示当前的首字母
                    //由于布局是复用的，所以在需要显示的时候，再次将first_word设置为可见
                    holder.first_word.setVisibility(View.VISIBLE);
                    holder.first_word.setText(currentWord);
                }
            } else {
                holder.first_word.setVisibility(View.VISIBLE);
                holder.first_word.setText(currentWord);
            }

            return convertView;
        }


    }

    static class ViewHolder {
        TextView name, first_word;

        public ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.name);
            first_word = (TextView) convertView.findViewById(R.id.first_word);
        }

        public static ViewHolder getHolder(View convertView) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }
}
