package com.zzhoujay.richtext.ig;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.zzhoujay.richtext.ImageHolder;
import com.zzhoujay.richtext.RichTextConfig;
import com.zzhoujay.richtext.callback.ImageLoadNotify;
import com.zzhoujay.richtext.drawable.DrawableWrapper;

import java.lang.ref.SoftReference;

/**
 * Created by zhou on 16-10-23.
 * ImageTarget Gif
 */
class ImageTargetGif extends ImageTarget<GifDrawable> implements Drawable.Callback {

    private SoftReference<GifDrawable> gifDrawableSoftReference;

    ImageTargetGif(TextView textView, DrawableWrapper drawableWrapper, ImageHolder holder, RichTextConfig config, ImageLoadNotify imageLoadNotify, Rect rect) {
        super(textView, drawableWrapper, holder, config, imageLoadNotify, rect);
    }


    @Override
    public void recycle() {
        Glide.clear(this);
        if (gifDrawableSoftReference != null) {
            GifDrawable gifDrawable = gifDrawableSoftReference.get();
            if (gifDrawable != null) {
                gifDrawable.setCallback(null);
                gifDrawable.stop();
            }
        }
    }

    @Override
    public void onResourceReady(GifDrawable resource, GlideAnimation<? super GifDrawable> glideAnimation) {
        if (!activityIsAlive()) {
            return;
        }
        DrawableWrapper drawableWrapper = urlDrawableWeakReference.get();
        if (drawableWrapper == null) {
            return;
        }
        gifDrawableSoftReference = new SoftReference<>(resource);
        Bitmap first = resource.getFirstFrame();
        holder.setImageState(ImageHolder.ImageState.READY);
        holder.setSize(first.getWidth(), first.getHeight());
        resource.setBounds(0, 0, first.getWidth(), first.getHeight());
        drawableWrapper.setDrawable(resource);

        if (config.imageFixCallback != null) {
            config.imageFixCallback.onImageReady(holder, first.getWidth(), first.getHeight());
        }
        if (rect != null) {
            resource.setBounds(rect);
        } else {
            if (config.autoFix || holder.isAutoFix() || !holder.isInvalidateSize()) {
                int width = getRealWidth();
                int height = (int) ((float) first.getHeight() * width / first.getWidth());
                drawableWrapper.setScaleType(ImageHolder.ScaleType.fit_auto);
                drawableWrapper.setBounds(0, 0, width, height);
            } else {
                drawableWrapper.setScaleType(holder.getScaleType());
                drawableWrapper.setBounds(0, 0, holder.getWidth(), holder.getHeight());
                drawableWrapper.setBorderHolder(holder.getBorderHolder());
            }
            drawableWrapper.calculate();

            if (holder.isAutoPlay()) {
                resource.setCallback(this);
                resource.start();
                resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
            }
        }
        resetText();
        loadDone();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        TextView textView = textViewWeakReference.get();
        if (textView != null) {
            textView.invalidate();
        } else {
            recycle();
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {

    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {

    }
}
