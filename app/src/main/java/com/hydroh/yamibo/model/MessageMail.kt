package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

data class MessageMail(
        val author: String,
        val authorAvatarUrl: String,
        val authorUid: String,
        val abstract: String,
        val replyTime: String,
        val messageCount: Int
) : MultiItemEntity {

    override fun getItemType(): Int = ItemType.TYPE_MESSAGE_MAIL
}