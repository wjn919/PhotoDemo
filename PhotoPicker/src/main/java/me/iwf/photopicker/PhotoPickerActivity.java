package me.iwf.photopicker;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.adapter.PopupDirectoryListAdapter;
import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.entity.PhotoDirectory;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;
import me.iwf.photopicker.utils.MediaStoreHelper;

import static android.widget.Toast.LENGTH_LONG;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.iwf.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.IS_EDITOR;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.REQUEST_EDITOR;
import static me.iwf.photopicker.fragment.PhotoPickerFragment.COUNT_MAX;

//展示图片列表的activity
public class PhotoPickerActivity extends AppCompatActivity {


    private PhotoPickerFragment pickerFragment;

    private ImagePagerFragment imagePagerFragment;
    //private MenuItem menuDoneItem;

    private int maxCount = DEFAULT_MAX_COUNT;

    /**
     * to prevent multiple calls to inflate menu
     */


    private boolean showGif = false;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;
    private ArrayList<String> originalPhotos = null;
    private TextView tvCancel;//取消按钮
    private TextView tvCatelogName;//目录
    private TextView tvContinue;//继续
    private ListPopupWindow listPopupWindow;
    private RequestManager mGlideRequestManager;
    //所有photos的路径
    private ArrayList<PhotoDirectory> directories;
    private PopupDirectoryListAdapter listAdapter;
    private LinearLayout llHeaderView;

    private boolean mIsEditor = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);//展示相机
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);//显示gif
        setShowGif(showGif);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);//获取最大图片数量
        columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);//获取列数
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);//获取已经选择图片的集合



        setContentView(R.layout.__picker_activity_photo_picker);

        tvCancel = (TextView) findViewById(R.id.tv_picker_cancel);
        tvCatelogName = (TextView) findViewById(R.id.tv_picker_catalog_name);
        tvContinue = (TextView) findViewById(R.id.tv_picker_continue);
        llHeaderView = (LinearLayout) findViewById(R.id.ll_header);

        //完成事件的处理
        setContinue();

        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
                intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
                startActivityForResult(intent, REQUEST_EDITOR);

            }
        });

        //取消事件的处理
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();


            }
        });


        //目录集合
        directories = new ArrayList<>();
        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();//commit()调用之后加上 executePendingTransactions()来保证立即执行, 即变异步为同步
        }

        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, showGif);
        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        directories.clear();
                        directories.addAll(dirs);
                        pickerFragment.updateCatelog(directories);
                        listAdapter.notifyDataSetChanged();

                    }
                });

        //设置目录
        setCatelog();
        tvCatelogName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    adjustHeight();
                    listPopupWindow.show();
                }
            }
        });

        //设置小方块的点击事件
        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

                tvContinue.setEnabled(selectedItemCount > 0);

                if (maxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                            LENGTH_LONG).show();
                    return false;
                }
                if (selectedItemCount == 0) {
                    tvContinue.setText(R.string.__picker_continue);
                } else {
                    tvContinue.setText(getString(R.string.__picker_continue_with_count, selectedItemCount, maxCount));
                }

                return true;
            }
        });


    }


    public void adjustHeight() {
        if (listAdapter == null) return;
        int count = listAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;
        if (listPopupWindow != null) {
            listPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height));
        }
    }

    private void setCatelog() {
        mGlideRequestManager = Glide.with(this);//使用glide
        listAdapter = new PopupDirectoryListAdapter(mGlideRequestManager, directories);//展示显示那些图片列表
        listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        listPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        listPopupWindow.setAnchorView(llHeaderView);
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);


        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();
                PhotoDirectory directory = directories.get(position);
                tvCatelogName.setText(directory.getName());
                pickerFragment.updateData(position);

            }
        });


    }

    //处理完成按钮
    private void setContinue() {
        if (originalPhotos != null && originalPhotos.size() > 0) {
            tvContinue.setEnabled(true);
            tvContinue.setText(
                    getString(R.string.__picker_continue_with_count, originalPhotos.size(), maxCount));
        } else {
            tvContinue.setEnabled(false);
        }

    }


    @Override
    public void onBackPressed() {
        if(mIsEditor){
            Intent intent = new Intent();
            intent.setClassName(getActivity(),"com.example.phototagdemo.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            mIsEditor = false;
        }else{
            super.onBackPressed();
        }

    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
   /* @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        ArrayList<String> selectedPath = imagePagerFragment.getSelectedPath();
                        //int type = imagePagerFragment.getStartType();
                        if (selectedPath != null) {
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPath);
                            pickerFragment.onActivityResult(REQUEST_IMAGE, RESULT_OK, intent);

                        }
                        getSupportFragmentManager().popBackStack();

                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }*/



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDITOR && RESULT_OK == resultCode) {
            if (data != null && data.getStringArrayListExtra(KEY_SELECTED_PHOTOS) != null) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS, data.getStringArrayListExtra(KEY_SELECTED_PHOTOS));
                setResult(RESULT_OK, intent);
                finish();
            }
        }if(resultCode==IS_EDITOR){
            if(data!=null&&data.getBooleanExtra("ISEDITOR",false)){
                setIsEditor(true);

            }

        }
    }


    public PhotoPickerActivity getActivity() {
        return this;
    }


    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }

    public boolean updapteContinue(int selectedItemCount, Photo photo) {
        tvContinue.setEnabled(selectedItemCount > 0);

        if (maxCount <= 1) {
            List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
            if (!photos.contains(photo.getPath())) {
                photos.clear();
                pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
            }
            return true;
        }

        if (selectedItemCount > maxCount) {
            Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                    LENGTH_LONG).show();
            return false;
        }
        if (selectedItemCount == 0) {
            tvContinue.setText(R.string.__picker_continue);
        } else {
            tvContinue.setText(getString(R.string.__picker_continue_with_count, selectedItemCount, maxCount));
        }
        return true;
    }


    public void updapteContinue() {

        List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
        tvContinue.setEnabled(photos.size() > 0);
        if (photos.size() > maxCount) {
            Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                    LENGTH_LONG).show();

        }
        if (photos.size() == 0) {
            tvContinue.setText(R.string.__picker_continue);

        } else {
            tvContinue.setText(getString(R.string.__picker_continue_with_count, photos.size(), maxCount));

        }

    }

    public void setIsEditor(boolean isEditor) {
        this.mIsEditor = isEditor;
    }
}
