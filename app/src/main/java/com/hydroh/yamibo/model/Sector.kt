package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

data class Sector(
        val title: String,
        val unreadNum: Int,
        val description: String,
        val url: String
) : MultiItemEntity {

    override fun getItemType(): Int {
        return ItemType.TYPE_SECTOR
    }
}

