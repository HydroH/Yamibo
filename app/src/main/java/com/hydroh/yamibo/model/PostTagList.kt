package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.common.ItemType

class PostTagList(
        val tagList: List<MultiItemEntity>,
        val selectedPos: Int
) : AbstractExpandableItem<MultiItemEntity>(), MultiItemEntity {

    override fun getItemType(): Int = ItemType.TYPE_TAG_LIST

    override fun getLevel(): Int = 0
}