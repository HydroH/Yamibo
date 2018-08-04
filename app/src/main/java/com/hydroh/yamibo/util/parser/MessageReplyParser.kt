package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.MessageReply
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.util.getUid
import com.hydroh.yamibo.util.toAvatarSize
import org.jsoup.nodes.Document

class MessageReplyParser {

    val replyList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        document.select("div.xld.xlda div.nts dl.cl").forEach {
            val avatarUrl = it.select("dd.m.avt.mbn a img").first()?.attr("src")
                    ?.toAvatarSize(UrlUtils.AvatarSize.MIDDLE)
                    ?: UrlUtils.getAvatarDefaultUrl(UrlUtils.AvatarSize.MIDDLE)
            val replyTime = it.select("dt span.xg1.xw0").first().ownText()
            it.select("dd.ntc_body").first().run {
                val elemTitle = select("a[target]:not([class])").first()
                val postTitle = elemTitle.ownText()
                val url = elemTitle.attr("href")
                val elemAuthor = select("a:not([target]):not([class])").first()
                val author = elemAuthor.ownText()
                val authorUid = elemAuthor.attr("href").getUid()

                replyList.add(MessageReply(postTitle, author, avatarUrl, authorUid, url, replyTime))
            }
        }
    }
}
