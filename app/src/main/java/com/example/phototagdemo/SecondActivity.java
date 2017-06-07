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


public class SecondActivity extends AppCompatActivity implements View.OnClickListener {



    private ArrayList<String> selectedPhotos = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private RecyclerView recyclerView;
    private ArrayList<String> select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();
        initEvent();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("ISEDITOR",true);
        setResult(PhotoPicker.IS_EDITOR,intent);
        finish();

    }

    private void initView() {
        select = getIntent().getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
        if(select!=null){
            selectedPhotos.addAll(select);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        photoAdapter = new PhotoAdapter(this, selectedPhotos);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);
    }
    private void initEvent() {
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                            PhotoPicker.builder()
                                    .setPhotoCount(PhotoAdapter.MAX)
                                    .setShowCamera(true)
                                    /*.setPreviewEnabled(false) 有这句话就photo不能点击看大图*/
                                    .setSelected(selectedPhotos)
                                    .start(SecondActivity.this);
                        } else {
                            PhotoPreview.builder()
                                    .setPhotos(selectedPhotos)
                                    .setCurrentItem(position)
                                    .start(SecondActivity.this);
                        }
                    }
                }));
    }

    @Override
    public void onClick(View v) {

    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();

            if (photos != null) {

                selectedPhotos.addAll(photos);
            }

        }
        if(resultCode==PhotoPicker.RESULT_CANCEL){
            selectedPhotos.clear();
        }
        photoAdapter.notifyDataSetChanged();
    }


}
