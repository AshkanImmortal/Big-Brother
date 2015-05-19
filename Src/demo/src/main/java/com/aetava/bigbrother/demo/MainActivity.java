package com.aetava.bigbrother.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.aetava.bigbrother.utils.ImageUtility;
import com.aetava.bigbrother.PhotoActivity;

public class MainActivity extends AppCompatActivity {

    private static final int GRAB_PHOTO = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == GRAB_PHOTO) {
            Uri photoUri = data.getData();
            // Get the bitmap in according to the width of the device
            Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(photoUri.getPath(), 1000, 1000);
            ((ImageView) findViewById(R.id.taken_image)).setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void grabPhoto(View view) {
        startActivityForResult(new Intent(MainActivity.this, PhotoActivity.class), GRAB_PHOTO);
    }
}
