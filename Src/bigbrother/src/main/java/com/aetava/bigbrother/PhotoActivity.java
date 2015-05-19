package com.aetava.bigbrother;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.aetava.bigbrother.model.Image;
import com.aetava.bigbrother.utils.ImageInternalFetcher;
import com.soundcloud.android.crop.Crop;

import java.io.File;

public class PhotoActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {

    public static final String TAG = PhotoActivity.class.getSimpleName();

    public ImageInternalFetcher mImageFetcher;

    private GalleryAdapter galleryAdapter;
    private View mTransparentView;
    private View mSpaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_photo);

        HeaderGridView galleryGridView = (HeaderGridView) findViewById(R.id.list);
        galleryGridView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);

        SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setEnableDragViewTouchEvents(true);

        galleryAdapter = new GalleryAdapter(this);

        int gapHeight = getResources().getDimensionPixelSize(R.dimen.map_height);
        slidingUpPanelLayout.setPanelHeight(gapHeight); // you can use different height here
        slidingUpPanelLayout.setScrollableView(galleryGridView, gapHeight);

        slidingUpPanelLayout.setPanelSlideListener(this);

        // transparent view at the top of ListView
        mTransparentView = findViewById(R.id.transparentView);

        // init header view for ListView
        View mTransparentHeaderView = LayoutInflater.from(this).inflate(R.layout.transparent_header_view, null, false);
        mSpaceView = mTransparentHeaderView.findViewById(R.id.space);

        galleryGridView.addHeaderView(mTransparentHeaderView);

        mImageFetcher = new ImageInternalFetcher(this, 1200);

        Cursor imageCursor = null;
        try {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns._ID};
            final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
            imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
            while (imageCursor.moveToNext()) {

                int columnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                Uri uri = Uri.parse(imageCursor.getString(columnIndex));
                //int orientation = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
                galleryAdapter.add(new Image(uri, 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(imageCursor != null && !imageCursor.isClosed()) {
                imageCursor.close();
            }
        }

        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //cropPhoto(galleryAdapter.getItem(i).uri);
                String path = "file://" + galleryAdapter.getItem(i).uri.toString();

                Uri uri = Uri.parse(path);
                cropPhoto(uri);
                galleryAdapter.notifyDataSetChanged();
            }
        });

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.camera_container, CameraFragment.newInstance());
            //fragmentTransaction.replace(R.id.gallery_container, galleryFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            returnPhotoUri(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void cropPhoto(Uri uri) {

        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(uri, destination).asSquare().start(this);
    }

    public void returnPhotoUri(Uri uri) {

        Intent data = new Intent();
        data.setData(uri);

        if (getParent() == null) {
            setResult(RESULT_OK, data);
        } else {
            getParent().setResult(RESULT_OK, data);
        }

        finish();
    }

    private void collapseCamera() {
        mSpaceView.setVisibility(View.VISIBLE);
        mTransparentView.setVisibility(View.GONE);
    }

    private void expandCamera() {
        mSpaceView.setVisibility(View.GONE);
        mTransparentView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPanelSlide(View view, float v) {

    }

    @Override
    public void onPanelCollapsed(View view) {
        expandCamera();
    }

    @Override
    public void onPanelExpanded(View view) {
        collapseCamera();
    }

    @Override
    public void onPanelAnchored(View view) {

    }
}
