package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

data class PrivateMessage(
        val author: String,
        val authorAvatarUrl: String,
        val authorUid: String,
        val time: String,
        val contentHtml: String,
        val isMe: Boolean
) : MultiItemEntity {

    override fun getItemType(): Int
            = if (isMe) ItemType.TYPE_PM_ME else ItemType.TYPE_PM_OTHER
}
