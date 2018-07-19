package com.hydroh.yamibo.ui.adapter

import android.content.Intent
import android.util.Log
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.ui.PostActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_POST

class SearchResultAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    companion object {
        private val TAG = this::class.java.simpleName
    }

    init {
        addItemType(TYPE_POST, R.layout.item_search_result)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_POST -> {
                val post = item as Post
                holder.setText(R.id.post_sector, post.sector)
                        .setText(R.id.post_title, post.title)
                        .setText(R.id.post_abstract, post.abstract)
                        .setText(R.id.post_replyNum, post.replyNum.toString())
                        .setText(R.id.post_datetime, post.postTime)
                        .setText(R.id.post_author, post.author)

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Search result position $position clicked.")
                    val clickedPost = mData[position] as Post
                    val intent = Intent(mContext, PostActivity::class.java)
                            .putExtra(Constants.ARG_INTENT_URL, clickedPost.url)
                            .putExtra(Constants.ARG_INTENT_TITLE, clickedPost.title)
                    mContext.startActivity(intent)
                }
            }
        }
    }
}