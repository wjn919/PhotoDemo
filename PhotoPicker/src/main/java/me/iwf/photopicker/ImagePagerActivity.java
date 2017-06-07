package me.iwf.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import me.iwf.photopicker.fragment.ImagePagerFragment;

/**
 * Created by wjn on 2017/6/6.
 */

public class ImagePagerActivity extends AppCompatActivity{
  //  private ImagePagerFragment imageFragment;
    private ArrayList<String> selectedPhotos;
    private ArrayList<String> photos;
    private int index;
    private ImagePagerFragment mImagePagerFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_image_pager);

        selectedPhotos = getIntent().getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
        photos =  getIntent().getStringArrayListExtra(PhotoPicker.ALL_PHOTOS);
        index = getIntent().getIntExtra("INDEX",0);
        int[] screenLocation = getIntent().getIntArrayExtra("SCREENLOCATION");
        int width = getIntent().getIntExtra("WIDTH",0);
        int height = getIntent().getIntExtra("HEIGHT",0);

        ImagePagerFragment imageFragment = (ImagePagerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (imageFragment == null) {

            ImagePagerFragment imagePagerFragment =
                    ImagePagerFragment.newInstance(selectedPhotos, photos, index, screenLocation, width,
                            height);
            mImagePagerFragment = imagePagerFragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_image, imagePagerFragment, "tag")
                    .commit();

        }
    }

    @Override
    public void onBackPressed() {
        if (mImagePagerFragment != null && mImagePagerFragment.isVisible()) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS,mImagePagerFragment.getSelectedPath());
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}
