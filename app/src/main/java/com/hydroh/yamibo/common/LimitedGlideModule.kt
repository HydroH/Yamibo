package com.hydroh.yamibo.common

import android.content.Context

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor
import com.bumptech.glide.module.GlideModule

class LimitedGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setResizeService(FifoPriorityThreadPoolExecutor(2))
    }

    override fun registerComponents(context: Context, glide: Glide) {}
}
