package com.hydroh.yamibo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.hydroh.yamibo.R;
import com.hydroh.yamibo.ui.adapter.ImageBrowserAdapter;

import java.util.List;

public class ImageGalleryActivity extends Activity {

    ViewPager imageBrowserPager;

    ImageBrowserAdapter adapter;
    String url;
    List<String> urlList;

    Toast toast;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        url = getIntent().getStringExtra("imgUrl");
        urlList = getIntent().getStringArrayListExtra("imgUrlList");

        int position = urlList.indexOf(url);
        adapter = new ImageBrowserAdapter(this, urlList);
        imageBrowserPager = findViewById(R.id.image_viewpager);
        imageBrowserPager.setAdapter(adapter);
        final int size = urlList.size();

        int index = position % size + 1;
        ShowToast(index + " / " + size);

        imageBrowserPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int index = position % size + 1;
                ShowToast(index + " / " + size);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        imageBrowserPager.setCurrentItem(position);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (toast != null) {
            toast.cancel();
        }
    }

    public void ShowToast(String text) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
