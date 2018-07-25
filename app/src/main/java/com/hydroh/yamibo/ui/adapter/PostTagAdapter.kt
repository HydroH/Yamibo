package com.hydroh.yamibo.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.model.PostTag
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType
import com.hydroh.yamibo.ui.common.PageReloadListener
import com.hydroh.yamibo.util.HtmlCompat

class PostTagAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    var pageReloadListener: PageReloadListener? = null

    companion object {
        private val TAG = this::class.java.simpleName
    }

    init {
        addItemType(ItemType.TYPE_TAG, R.layout.item_tag)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            ItemType.TYPE_TAG -> {
                (item as PostTag).run {
                    val postFix = if (postNum > 0) " " + postNum.toString() else ""
                    val spanned = HtmlCompat.fromHtml("$title<b>$postFix</b>")
                    holder.setText(R.id.button_tag, spanned)
                            .setTextColor(R.id.button_tag, Color.WHITE)
                    holder.itemView.isEnabled = !selected
                    holder.itemView.alpha = if (selected) .5f else 1f
                }

                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as PostTag).run {
                        pageReloadListener?.onPageReload(url)
                    }
                }
            }
        }
    }
}