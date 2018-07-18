package com.hydroh.yamibo.ui.common

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

abstract class AbsMultiAdapter(data: List<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
    fun clear() {
        mData.clear()
        notifyDataSetChanged()
    }
}