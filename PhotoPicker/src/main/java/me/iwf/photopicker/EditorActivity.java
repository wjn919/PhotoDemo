package me.iwf.photopicker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import me.iwf.photopicker.adapter.PhotoEditorPagerAdapter;

/**
 * Created by wjn on 2017/6/5.
 */

public class EditorActivity extends Activity{

    ViewPager mViewPager;
    private ArrayList selected_photo_path;
    private PhotoEditorPagerAdapter mPagerAdapter;
    private LinearLayout mReturn;
    private TextView mCatelog;
    private TextView mContinue;
    private int currentItem = 1;
    private boolean isEditor = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_editor);
        selected_photo_path = getIntent().getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
        initView();
        setTitle();
    }

    private void setTitle() {

        mCatelog.setText(getString(R.string.__picker_editor,currentItem,selected_photo_path.size()));
    }


    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_photos);
        mReturn = (LinearLayout) findViewById(R.id.tv_picker_return);
        mCatelog = (TextView) findViewById(R.id.tv_picker_catalog_name);
        mContinue = (TextView) findViewById(R.id.tv_picker_continue);
        mPagerAdapter = new PhotoEditorPagerAdapter(Glide.with(this), selected_photo_path);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(5);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentItem = position+1;
                setTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });

        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName(EditorActivity.this,"com.example.phototagdemo.SecondActivity");
                intent.putStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS,selected_photo_path);
                startActivityForResult(intent,PhotoPicker.REQUEST_CODE);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==PhotoPicker.IS_EDITOR){
            if(data!=null&&data.getBooleanExtra("ISEDITOR",false)){
                isEditor = true;

            }
        }
    }

    @Override
    public void onBackPressed() {
        if(isEditor){
            Intent intent = new Intent();
            intent.putExtra("ISEDITOR",true);
            setResult(PhotoPicker.IS_EDITOR,intent);
            isEditor = false;
            finish();
        }else {
            super.onBackPressed();
        }


    }
}
