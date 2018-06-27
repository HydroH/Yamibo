package com.hydroh.yamibo.ui.adapter;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.hydroh.yamibo.R;
import com.zzhoujay.richtext.cache.BitmapPool;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

public class ImageBrowserAdapter extends PagerAdapter {
    private Activity context;
    private List<String> imgUrlList;
    private HashMap<String, String> urlKeyMap;

    public ImageBrowserAdapter(Activity context, List<String> imgUrlList, HashMap<String, String> urlKeyMap) {
        this.context = context;
        this.imgUrlList = imgUrlList;
        this.urlKeyMap = urlKeyMap;
    }

    @Override
    public int getCount() {
        return imgUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View view = View.inflate(context, R.layout.item_img_browser, null);
        ImageView imageBrowserView = view.findViewById(R.id.image_browser_view);
        String imgUrl = imgUrlList.get(position);
        final PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageBrowserView);
        String holderKey = urlKeyMap.get(imgUrl);

        RequestBuilder<Drawable> glideRequestBuilder;
        if (holderKey != null && BitmapPool.getPool().hasBitmapLocalCache(holderKey)) {
            Bitmap bitmapCache = BitmapPool.getPool().getBitmap(holderKey);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapCache.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes=baos.toByteArray();
            glideRequestBuilder = Glide.with(context)
                    .load(bytes)
                    .apply(new RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    );

        } else {
            glideRequestBuilder = Glide.with(context)
                    .load(imgUrl);
        }
        glideRequestBuilder.transition(new DrawableTransitionOptions().crossFade())
                .into(new DrawableImageViewTarget(imageBrowserView) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> animation) {
                        super.onResourceReady(resource, animation);
                        photoViewAttacher.update();
                    }
                });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
