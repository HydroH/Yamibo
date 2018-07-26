package com.hydroh.yamibo.ui.adapter

import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.ui.PostActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_POST
import org.jetbrains.anko.startActivity

class FavoriteAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    init {
        addItemType(TYPE_POST, R.layout.item_favorite)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_POST -> {
                holder.setText(R.id.favorite_post_title, (item as Post).title)

                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as Post).run {
                        mContext.startActivity<PostActivity>(
                                Constants.ARG_INTENT_URL to url,
                                Constants.ARG_INTENT_TITLE to title
                        )
                    }
                }
            }
        }
    }
}