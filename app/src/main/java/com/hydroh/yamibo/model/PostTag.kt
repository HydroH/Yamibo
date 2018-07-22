package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

data class PostTag(
        val title: String,
        val url: String,
        val postNum: Int,
        val selected: Boolean
) : MultiItemEntity {

    override fun getItemType(): Int = ItemType.TYPE_TAG
}