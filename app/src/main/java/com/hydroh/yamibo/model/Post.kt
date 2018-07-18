package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

data class Post(
        val title: String,
        val tag: String,
        val author: String,
        val postTime: String,
        val replyNum: Int,
        val url: String,
        val sector: String
) : MultiItemEntity {

    override fun getItemType(): Int {
        return ItemType.TYPE_POST
    }
}
