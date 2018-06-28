package com.hydroh.yamibo.model

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.ui.adapter.HomeAdapter

class SectorGroup(
        val title: String
) : AbstractExpandableItem<MultiItemEntity>(), MultiItemEntity {

    override fun getItemType(): Int {
        return HomeAdapter.TYPE_GROUP
    }

    override fun getLevel(): Int {
        return 0
    }
}
