package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

data class ReplyMini(
        val text: String,
        val url: String
) : MultiItemEntity {

    override fun getItemType(): Int {
        return ItemType.TYPE_REPLY_MINI
    }
}
