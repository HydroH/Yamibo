package com.hydroh.yamibo.ui.adaptor;


import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.hydroh.yamibo.R;

import java.util.List;

public class ImageBrowserAdapter extends PagerAdapter {
    private Activity context;
    private List<String> imgUrlList;

    public ImageBrowserAdapter(Activity context, List<String> imgUrlList) {
        this.context = context;
        this.imgUrlList = imgUrlList;
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
        ImageView imageBrowserView = (ImageView) view.findViewById(R.id.image_browser_view);
        String imgUrl = imgUrlList.get(position);
        final PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageBrowserView);
        Glide.with(context)
                .load(imgUrl)
                .crossFade()
                .into(new GlideDrawableImageViewTarget(imageBrowserView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
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
