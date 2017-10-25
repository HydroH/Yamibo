package com.hydroh.yamibo.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hydroh.yamibo.ui.ImageGalleryActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class JavascriptInterface {
    private Context context;
    private List<String> imgUrlList;

    public JavascriptInterface(Context context) {
        this.context = context;
        this.imgUrlList = new ArrayList<>();
    }

    public JavascriptInterface(Context context, List<String> imgUrlList) {
        this.context = context;
        this.imgUrlList = imgUrlList;
    }

    @android.webkit.JavascriptInterface
    public void openImage(String url) {
        Log.d(TAG, "openImage: " + url);
        Intent intent = new Intent();
        intent.putExtra("imgUrl", url);
        intent.putStringArrayListExtra("imgUrlList", (ArrayList<String>) imgUrlList);
        intent.setClass(context, ImageGalleryActivity.class);
        context.startActivity(intent);
    }
}
