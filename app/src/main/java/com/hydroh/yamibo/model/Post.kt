package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.adapter.HomeAdapter

data class Post(
        val title: String,
        val tag: String,
        val author: String,
        val replyNum: Int,
        val url: String
) : MultiItemEntity {

    override fun getItemType(): Int {
        return HomeAdapter.TYPE_THREAD
    }
}
