package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.MessageMail
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.util.getUid
import com.hydroh.yamibo.util.toAvatarSize
import com.hydroh.yamibo.util.toIntOrNullIgnore
import org.jsoup.nodes.Document

class MessageMailParser {

    val mailList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        document.select("dl.bbda.cur1.cl").forEach {
            val avatarUrl = it.select("a img").first()?.attr("src")
                    ?.toAvatarSize(UrlUtils.AvatarSize.MIDDLE)
                    ?: UrlUtils.getAvatarDefaultUrl(UrlUtils.AvatarSize.MIDDLE)
            it.select("dd.ptm.pc_c").first().run {
                val elemAuthor = select("a.xw1").first()
                val author = elemAuthor.ownText()
                val authorUid = elemAuthor.attr("href").getUid()
                val abstract = ownText()
                val replyTime = select("span.xg1").first().ownText()
                val messageCount = select("span.pm_o.y span.xg1.z").first().ownText().toIntOrNullIgnore() ?: 0

                mailList.add(MessageMail(author, avatarUrl, authorUid, abstract, replyTime, messageCount))
            }
        }
    }
}