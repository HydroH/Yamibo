package com.hydroh.yamibo.common;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.bumptech.glide.module.GlideModule;

public class LimitedGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setResizeService(new FifoPriorityThreadPoolExecutor(2));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}
