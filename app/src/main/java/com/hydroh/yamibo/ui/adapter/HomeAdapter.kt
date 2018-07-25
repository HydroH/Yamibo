package com.hydroh.yamibo.ui.adapter

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
import org.jetbrains.anko.startActivity

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
                holder.setNestView(R.id.view_list_tag)
                holder.getView<RecyclerView>(R.id.list_tag).run {
                    layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                    setHasFixedSize(true)
                    (item as PostTagList).let {
                        adapter = PostTagAdapter(it.tagList).also { it.pageReloadListener = pageReloadListener }
                        scrollToPosition(it.selectedPos)
                    }
                }
            }

            TYPE_GROUP -> {
                val group = item as SectorGroup
                if (group.isExpanded) {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less)
                } else {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more)
                }.setText(R.id.group_title, group.title)

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
                (item as Sector).run {
                    if (unreadNum > 0) {
                        holder.setText(R.id.sector_unread_num, unreadNum.toString())
                    } else {
                        holder.setBackgroundRes(R.id.sector_unread_num, R.drawable.none)
                    }.setText(R.id.sector_title, title)
                            .setText(R.id.sector_description, description)
                }

                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as Sector).run {
                        mContext.startActivity<SectorActivity>(
                                Constants.ARG_INTENT_URL to url,
                                Constants.ARG_INTENT_TITLE to title
                        )
                    }

                }
            }

            TYPE_POST -> {
                (item as Post).run {
                    val spanned = HtmlCompat.fromHtml("<font color='#0099cc'>$tag</font>$title")
                    holder.setText(R.id.post_title, spanned)
                            .setText(R.id.post_author, author)
                            .setText(R.id.post_datetime, postTime)
                            .setText(R.id.post_replyNum, replyNum.toString())
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
        }
    }

    fun collapseSticky() {
        mData.forEachIndexed { index, data ->
            if (data is SectorGroup && data.title.contains("置顶")) {
                collapse(index)
                return
            }
        }
    }
}
