package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.adapter.HomeAdapter

data class Sector(
        val title: String,
        val unreadNum: Int,
        val description: String,
        val url: String
) : MultiItemEntity {

    override fun getItemType(): Int {
        return HomeAdapter.TYPE_SECTOR
    }
}

