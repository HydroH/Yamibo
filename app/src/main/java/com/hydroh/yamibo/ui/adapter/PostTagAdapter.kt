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
                val postTag = item as PostTag
                var postFix = ""
                if (postTag.postNum > 0) postFix = " " + postTag.postNum.toString()
                val spanned = HtmlCompat.fromHtml("${postTag.title}<b>$postFix</b>")
                holder.setText(R.id.button_tag, spanned)
                if (postTag.selected) {
                    holder.itemView.isEnabled = false
                    holder.itemView.alpha = .5f
                    holder.setTextColor(R.id.button_tag, Color.WHITE)
                } else {
                    holder.itemView.isEnabled = true
                    holder.itemView.alpha = 1f
                }

                holder.itemView.setOnClickListener {
                    val clickedTag = mData[holder.adapterPosition] as PostTag
                    pageReloadListener?.onPageReload(clickedTag.url)
                }
            }
        }
    }
}