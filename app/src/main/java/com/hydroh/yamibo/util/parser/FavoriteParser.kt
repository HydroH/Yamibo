package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.Post
import org.jsoup.nodes.Document

class FavoriteParser {

    var nextPageUrl: String? = null
        private set
    val favoriteList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        nextPageUrl = document.select("div.pg a.nxt").first()?.attr("href")

        document.select("ul#favorite_ul li.bbda.ptm.pbm").forEach {
            it.select("a:not(.y)").first().run {
                val title = ownText()
                val url = attr("href")
                favoriteList.add(Post(title, "", "", "", 0, url, "", ""))
            }
        }
    }
}