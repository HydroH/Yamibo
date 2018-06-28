package com.zzhoujay.richtext.ig;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.zzhoujay.richtext.ImageHolder;
import com.zzhoujay.richtext.RichTextConfig;
import com.zzhoujay.richtext.callback.ImageLoadNotify;
import com.zzhoujay.richtext.drawable.DrawableWrapper;

/**
 * Created by zhou on 16-10-23.
 * ImageTarget Bitmap
 */
class ImageTargetBitmap extends ImageTarget<Bitmap> {


    ImageTargetBitmap(TextView textView, DrawableWrapper drawableWrapper, ImageHolder holder, RichTextConfig config, ImageLoadNotify imageLoadNotify, Rect rect) {
        super(textView, drawableWrapper, holder, config, imageLoadNotify, rect);
    }

    @Override
    public void recycle() {
        Glide.clear(this);
    }

    @Override
    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
        if (!activityIsAlive()) {
            return;
        }
        DrawableWrapper drawableWrapper = urlDrawableWeakReference.get();
        if (drawableWrapper == null) {
            return;
        }
        TextView textView = textViewWeakReference.get();
        holder.setImageState(ImageHolder.ImageState.READY);
        holder.setSize(resource.getWidth(), resource.getHeight());
        Drawable drawable = new BitmapDrawable(textView.getResources(), resource);
        drawable.setBounds(0, 0, resource.getWidth(), resource.getHeight());
        drawableWrapper.setDrawable(drawable);

        if (config.imageFixCallback != null) {
            config.imageFixCallback.onImageReady(holder, resource.getWidth(), resource.getHeight());
        }
        if (rect != null) {
            drawable.setBounds(rect);
        } else {
            if (config.autoFix || holder.isAutoFix() || !holder.isInvalidateSize()) {
                int width = getRealWidth();
                int height = (int) ((float) resource.getHeight() * width / resource.getWidth());
                drawableWrapper.setScaleType(ImageHolder.ScaleType.fit_auto);
                drawableWrapper.setBounds(0, 0, width, height);
            } else {
                drawableWrapper.setScaleType(holder.getScaleType());
                drawableWrapper.setBounds(0, 0, holder.getWidth(), holder.getHeight());
                drawableWrapper.setBorderHolder(holder.getBorderHolder());
            }
            drawableWrapper.calculate();
        }
        resetText();
        loadDone();
    }
}

