package com.hydroh.yamibo.ui.adapter

import android.content.Intent
import android.text.Html
import android.util.Log
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.Sector
import com.hydroh.yamibo.model.SectorGroup
import com.hydroh.yamibo.ui.HomeActivity
import com.hydroh.yamibo.ui.PostActivity

class HomeAdapter(data: List<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    companion object {
        private val TAG = HomeAdapter::class.java.simpleName

        const val TYPE_GROUP = 0
        const val TYPE_SECTOR = 1
        const val TYPE_THREAD = 2
    }

    init {
        addItemType(TYPE_GROUP, R.layout.item_group)
        addItemType(TYPE_SECTOR, R.layout.item_sector)
        addItemType(TYPE_THREAD, R.layout.item_post)
    }

    @Suppress("DEPRECATION")
    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_GROUP -> {
                val group = item as SectorGroup
                holder.setText(R.id.group_title, group.title)

                if (group.isExpanded) {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less_black_24dp)
                } else {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more_black_24dp)
                }

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Sector group position $position clicked.")
                    if (group.isExpanded) {
                        collapse(position)
                        holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more_black_24dp)
                    } else {
                        expand(position)
                        holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less_black_24dp)
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
                    val intent = Intent(mContext, HomeActivity::class.java)
                    intent.putExtra("url", clickedSector.url)
                    intent.putExtra("title", clickedSector.title)
                    mContext.startActivity(intent)
                }
            }

            TYPE_THREAD -> {
                val post = item as Post
                val spanned = Html.fromHtml("<font color='#0099cc'>${post.tag}</font>${post.title}")
                holder.setText(R.id.post_title, spanned)
                        .setText(R.id.post_author, post.author)
                        .setText(R.id.post_replyNum, post.replyNum.toString())

                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    Log.d(TAG, "Post position $position clicked.")
                    val clickedPost = mData[position] as Post
                    val intent = Intent(mContext, PostActivity::class.java)
                    intent.putExtra("url", clickedPost.url)
                    intent.putExtra("title", clickedPost.title)
                    mContext.startActivity(intent)
                }
            }
        }
    }

    fun clear() {
        mData.clear()
        notifyDataSetChanged()
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
