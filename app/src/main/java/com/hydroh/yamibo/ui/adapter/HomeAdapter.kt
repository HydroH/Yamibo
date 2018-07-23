package com.hydroh.yamibo.ui.adapter

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.PostTagList
import com.hydroh.yamibo.model.Sector
import com.hydroh.yamibo.model.SectorGroup
import com.hydroh.yamibo.ui.PostActivity
import com.hydroh.yamibo.ui.SectorActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_GROUP
import com.hydroh.yamibo.ui.common.ItemType.TYPE_POST
import com.hydroh.yamibo.ui.common.ItemType.TYPE_SECTOR
import com.hydroh.yamibo.ui.common.ItemType.TYPE_TAG_LIST
import com.hydroh.yamibo.ui.common.PageReloadListener
import com.hydroh.yamibo.util.HtmlCompat

class HomeAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    var pageReloadListener: PageReloadListener? = null

    companion object {
        private val TAG = this::class.java.simpleName
    }

    init {
        addItemType(TYPE_TAG_LIST, R.layout.item_tag_list)
        addItemType(TYPE_GROUP, R.layout.item_group)
        addItemType(TYPE_SECTOR, R.layout.item_sector)
        addItemType(TYPE_POST, R.layout.item_post)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_TAG_LIST -> {
                val tagList = item as PostTagList
                holder.setNestView(R.id.view_list_tag)
                val tagRecyclerView = holder.getView<RecyclerView>(R.id.list_tag)
                val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                tagRecyclerView.layoutManager = layoutManager
                tagRecyclerView.setHasFixedSize(true)

                val tagAdapter = PostTagAdapter(tagList.tagList)
                tagAdapter.pageReloadListener = pageReloadListener
                tagRecyclerView.adapter = tagAdapter
                tagRecyclerView.scrollToPosition(tagList.selectedPos)
            }

            TYPE_GROUP -> {
                val group = item as SectorGroup
                holder.setText(R.id.group_title, group.title)

                if (group.isExpanded) {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less)
                } else {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more)
                }

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Sector group position $position clicked.")
                    if (group.isExpanded) {
                        collapse(position)
                        holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more)
                    } else {
                        expand(position)
                        holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less)
                    }
                }
            }

            TYPE_SECTOR -> {
                val sector = item as Sector
                holder.setText(R.id.sector_title, sector.title)
                if (sector.unreadNum > 0) {
                    holder.setText(R.id.sector_unread_num, sector.unreadNum.toString())
                } else {
                    holder.setBackgroundRes(R.id.sector_unread_num, R.drawable.none)
                }
                holder.setText(R.id.sector_description, sector.description)

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Sector position $position clicked.")
                    val clickedSector = mData[position] as Sector
                    val intent = Intent(mContext, SectorActivity::class.java)
                            .putExtra(Constants.ARG_INTENT_URL, clickedSector.url)
                            .putExtra(Constants.ARG_INTENT_TITLE, clickedSector.title)
                    mContext.startActivity(intent)
                }
            }

            TYPE_POST -> {
                val post = item as Post
                val spanned = HtmlCompat.fromHtml("<font color='#0099cc'>${post.tag}</font>${post.title}")
                holder.setText(R.id.post_title, spanned)
                        .setText(R.id.post_author, post.author)
                        .setText(R.id.post_datetime, post.postTime)
                        .setText(R.id.post_replyNum, post.replyNum.toString())

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Post position $position clicked.")
                    val clickedPost = mData[position] as Post
                    val intent = Intent(mContext, PostActivity::class.java)
                            .putExtra(Constants.ARG_INTENT_URL, clickedPost.url)
                            .putExtra(Constants.ARG_INTENT_TITLE, clickedPost.title)
                    mContext.startActivity(intent)
                }
            }
        }
    }

    fun collapseSticky() {
        for (i in mData.indices) {
            if (mData[i] is SectorGroup && (mData[i] as SectorGroup).title.contains("置顶")) {
                collapse(i)
                break
            }
        }

    }
}
