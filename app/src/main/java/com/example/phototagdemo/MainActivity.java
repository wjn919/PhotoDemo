package com.example.phototagdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.example.phototagdemo.adapter.PhotoAdapter;
import com.example.phototagdemo.listener.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvaddpic;//添加图片

    private ArrayList<String> selectedPhotos = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }



    private void initView() {
        tvaddpic = (TextView) findViewById(R.id.tv_add_pic);

    }
    private void initEvent() {
        tvaddpic.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v==tvaddpic){//添加图片,不记录选中的照片
            PhotoPicker.builder()
                    .setPhotoCount(9)
                    .setGridColumnCount(4)
                    .setShowCamera(true)
                    .start(MainActivity.this);


        }
    }





}
