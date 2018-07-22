package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.ReplyMini
import org.jsoup.nodes.Document

class ProfileListParser {

    var nextPageUrl: String? = null
        private set
    val profilePostList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        document.select("div.pgs.cl.mtm div.pg a").first()?.run {
            if (ownText() == "下一页") {
                nextPageUrl = attr("href")
            }
        }
        document.select("div.bm_c").first()?.run {
            select("div.tl tbody tr")
                    .filter { it.attr("class") != "th" }
                    .forEach {
                        if (it.select("td.icn").first() != null) {
                            val elemTitle = it.select("th a").first()
                            val title = elemTitle.ownText()
                            val url = elemTitle.attr("href")
                            val sector = it.select("td a.xg1").first().ownText()
                            val replyNum = it.select("td.num a.xi2").first().ownText().toIntOrNull()
                                    ?: 0
                            val author = it.select("td.by cite a").first().ownText()
                            val postTime = it.select("td.by em a").first().ownText()
                            profilePostList.add(Post(title, "", author, postTime, replyNum, url, sector, ""))
                        } else {
                            val elemReply = it.select("td.xg1 a").first()
                            val text = elemReply.ownText()
                            val url = elemReply.attr("href")
                            profilePostList.add(ReplyMini(text, url))
                        }
                    }
        }
    }
}