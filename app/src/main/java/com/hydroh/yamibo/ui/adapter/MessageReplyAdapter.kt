package com.hydroh.yamibo.ui.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.MessageReply
import com.hydroh.yamibo.ui.PostActivity
import com.hydroh.yamibo.ui.ProfileActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_MESSAGE_REPLY
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.startActivity

class MessageReplyAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    init {
        addItemType(TYPE_MESSAGE_REPLY, R.layout.item_message_reply)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_MESSAGE_REPLY -> {
                (item as MessageReply).run {
                    Glide.with(mContext).load(authorAvatarUrl).crossFade()
                            .into(holder.getView(R.id.message_reply_avatar))

                    holder.setText(R.id.message_reply_author, "$author 回复了您的帖子")
                            .setText(R.id.message_reply_title, postTitle)
                            .setText(R.id.message_reply_time, replyTime)
                }
                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as MessageReply).run {
                        mContext.startActivity<PostActivity>(
                                Constants.ARG_INTENT_URL to url,
                                Constants.ARG_INTENT_TITLE to postTitle
                        )
                    }
                }
                holder.getView<CircleImageView>(R.id.message_reply_avatar).setOnClickListener {
                    (mData[holder.adapterPosition] as MessageReply).run {
                        mContext.startActivity<ProfileActivity>(
                                Constants.ARG_INTENT_UID to authorUid,
                                Constants.ARG_INTENT_USERNAME to author,
                                Constants.ARG_INTENT_AVATAR_URL to authorAvatarUrl
                        )
                    }
                }
            }
        }
    }
}