package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.Post
import org.jsoup.nodes.Document

class SearchResultParser {

    var nextPageUrl: String? = null
        private set
    val postList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        nextPageUrl = document.select("div.pg a.nxt").first()?.attr("href")
        document.select("div#threadlist ul li.pbw").forEach {
            val elemTitle = it.select("h3.xs3 a").first()
            val title = elemTitle.text()
            val url = elemTitle.attr("href")
            val replyNumRaw = it.select("p.xg1").first().ownText()
            val replyNum = Regex("^\\d*").find(replyNumRaw)?.value?.toIntOrNull() ?: 0
            val abstract = it.select("p:nth-child(3)").first()?.ownText() ?: ""
            it.select("p:nth-child(4)").first().run {
                val postTime = it.select("span:nth-child(1)").first().ownText()
                val author = it.select("span:nth-child(2) a").first().ownText()
                val sector = it.select("span:nth-child(3) a.xi1").first().ownText()

                postList.add(Post(title, "", author, postTime, replyNum, url, sector, abstract))
            }
        }
    }
}