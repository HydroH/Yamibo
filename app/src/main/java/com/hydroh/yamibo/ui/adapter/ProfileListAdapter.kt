package com.hydroh.yamibo.ui.adapter

import android.content.Intent
import android.util.Log
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.ReplyMini
import com.hydroh.yamibo.ui.PostActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_POST
import com.hydroh.yamibo.ui.common.ItemType.TYPE_REPLY_MINI

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
                val post = item as Post
                holder.setText(R.id.post_sector, post.sector)
                        .setText(R.id.post_title, post.title)
                        .setText(R.id.post_replyNum, post.replyNum.toString())
                        .setText(R.id.post_datetime, post.postTime)
                        .setText(R.id.post_author, post.author)

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Sector position $position clicked.")
                    val clickedPost = mData[position] as Post
                    val intent = Intent(mContext, PostActivity::class.java)
                            .putExtra("url", clickedPost.url)
                            .putExtra("title", clickedPost.title)
                    mContext.startActivity(intent)
                }

            }
            TYPE_REPLY_MINI -> {
                val replyMini = item as ReplyMini
                holder.setText(R.id.reply_content, replyMini.text)

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Sector position $position clicked.")
                    val clickedPost = mData[position] as ReplyMini
                    val intent = Intent(mContext, PostActivity::class.java)
                            .putExtra("url", clickedPost.url)
                    mContext.startActivity(intent)
                }
            }
        }
    }
}