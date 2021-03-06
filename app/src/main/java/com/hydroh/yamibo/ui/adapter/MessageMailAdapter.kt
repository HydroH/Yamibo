package com.hydroh.yamibo.ui.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.MessageMail
import com.hydroh.yamibo.ui.PrivateMessageActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_MESSAGE_MAIL
import org.jetbrains.anko.startActivity

class MessageMailAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    init {
        addItemType(TYPE_MESSAGE_MAIL, R.layout.item_message_mail)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_MESSAGE_MAIL -> {
                (item as MessageMail).run {
                    Glide.with(mContext).load(authorAvatarUrl).crossFade()
                            .into(holder.getView(R.id.message_mail_avatar))

                    holder.setText(R.id.message_mail_author, author)
                            .setText(R.id.message_mail_abstract, abstract)
                }

                holder.itemView.setOnClickListener {
                    (mData[holder.adapterPosition] as MessageMail).run {
                        mContext.startActivity<PrivateMessageActivity>(
                                Constants.ARG_INTENT_UID to authorUid,
                                Constants.ARG_INTENT_USERNAME to author
                        )
                    }
                }
            }
        }
    }
}