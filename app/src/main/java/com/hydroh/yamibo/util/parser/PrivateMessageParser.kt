package com.hydroh.yamibo.util.parser

import android.content.ContentValues.TAG
import android.util.Log
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.PrivateMessage
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.util.toAvatarSize
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.jsoup.nodes.Document

class PrivateMessageParser {

    val messageList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        document.select("div#pm_ul dl.bbda.cl").forEachReversedByIndex {
            val avatarUrl = it.select("dd.m.avt a img").first()?.attr("src")
                    ?.toAvatarSize(UrlUtils.AvatarSize.MIDDLE)
                    ?: UrlUtils.getAvatarDefaultUrl(UrlUtils.AvatarSize.MIDDLE)
            it.select("dd.ptm").first().run {
                val elemAuthor = select(".xw1").first()
                val author = elemAuthor.ownText()
                var isMe = false
                val authorUid = if (elemAuthor.hasAttr("href")) {
                    elemAuthor.attr("href")
                } else {
                    isMe = true
                    ""
                }
                elemAuthor.remove()
                val elemTime = select("span.xg1").first()
                val time = elemTime.ownText()
                elemTime.remove()
                val contentHtml = html().replace("^\\s*&nbsp;\\s*<br>".toRegex(RegexOption.MULTILINE), "")
                Log.d(TAG, ": $contentHtml")
                messageList.add(PrivateMessage(author, avatarUrl, authorUid, time, contentHtml, isMe))
            }
        }
    }
}