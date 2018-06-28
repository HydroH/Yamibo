package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.adapter.PostAdapter

data class Reply(
        val author: String,
        val avatarUrl: String,
        val contentHTML: String,
        val postDate: String,
        val floorNum: Int
) : MultiItemEntity {

    override fun getItemType(): Int {
        return PostAdapter.TYPE_REPLY
    }
}
