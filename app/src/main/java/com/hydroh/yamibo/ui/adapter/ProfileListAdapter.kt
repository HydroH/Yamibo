package com.hydroh.yamibo.ui.adapter

import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.ReplyMini
import com.hydroh.yamibo.ui.PostActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_POST
import com.hydroh.yamibo.ui.common.ItemType.TYPE_REPLY_MINI
import org.jetbrains.anko.startActivity

class ProfileListAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data)  {

    companion object {
        private val TAG = this::class.java.simpleName
    }

    init {
        addItemType(TYPE_POST, R.layout.item_profile_post)
        addItemType(TYPE_REPLY_MINI, R.layout.item_profile_reply)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_POST -> {
                (item as Post).run {
                    holder.setText(R.id.post_sector, sector)
                            .setText(R.id.post_title, title)
                            .setText(R.id.post_replyNum, replyNum.toString())
                            .setText(R.id.post_datetime, postTime)
                            .setText(R.id.post_author, author)
                }

                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as Post).run {
                        mContext.startActivity<PostActivity>(
                                Constants.ARG_INTENT_URL to url,
                                Constants.ARG_INTENT_TITLE to title
                        )
                    }
                }

            }
            TYPE_REPLY_MINI -> {
                holder.setText(R.id.reply_content, (item as ReplyMini).text)

                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as ReplyMini).run {
                        mContext.startActivity<PostActivity>(
                                Constants.ARG_INTENT_URL to url
                        )
                    }
                }
            }
        }
    }
}