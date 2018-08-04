package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

class MessageReply(
        val postTitle: String,
        val author: String,
        val authorAvatarUrl: String,
        val authorUid: String,
        val url: String,
        val replyTime: String
) : MultiItemEntity {

    override fun getItemType(): Int = ItemType.TYPE_MESSAGE_REPLY
}