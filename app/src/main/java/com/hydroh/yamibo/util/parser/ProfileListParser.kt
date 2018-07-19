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
        nextPageUrl = document.select("div.pgs.cl.mtm div.pg a").first()?.attr("href")
        document.select("div.bm_c").first()?.run {
            for (elemPost in select("div.tl tbody tr")) {
                if (elemPost.attr("class") == "th") continue
                if (elemPost.select("td.icn").first() != null) {
                    val elemTitle = elemPost.select("th a").first()
                    val title = elemTitle.ownText()
                    val url = elemTitle.attr("href")
                    val sector = elemPost.select("td a.xg1").first().ownText()
                    val replyNum = elemPost.select("td.num a.xi2").first().ownText().toIntOrNull()
                            ?: 0
                    val author = elemPost.select("td.by cite a").first().ownText()
                    val postTime = elemPost.select("td.by em a").first().ownText()
                    profilePostList.add(Post(title, "", author, postTime, replyNum, url, sector, ""))
                } else {
                    val elemReply = elemPost.select("td.xg1 a").first()
                    val text = elemReply.ownText()
                    val url = elemReply.attr("href")
                    profilePostList.add(ReplyMini(text, url))
                }
            }
        }
    }
}