package com.hydroh.yamibo.ui;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hydroh.yamibo.R;
import com.hydroh.yamibo.ui.adaptor.ImageBrowserAdapter;

import java.util.List;

import static android.content.ContentValues.TAG;

public class ImageGalleryActivity extends AppCompatActivity {

    ViewPager imageBrowserPager;

    ImageBrowserAdapter adapter;
    String url;
    List<String> urlList;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        url = getIntent().getStringExtra("imgUrl");
        urlList = getIntent().getStringArrayListExtra("imgUrlList");

        int position = urlList.indexOf(url);
        adapter = new ImageBrowserAdapter(this, urlList);
        imageBrowserPager = (ViewPager) findViewById(R.id.image_viewpager);
        imageBrowserPager.setAdapter(adapter);

        imageBrowserPager.setCurrentItem(position);
    }
}
