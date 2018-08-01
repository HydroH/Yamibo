package com.hydroh.yamibo.util.parser

import android.content.ContentValues
import android.util.Log
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.Reply
import com.hydroh.yamibo.network.UrlUtils
import org.jsoup.nodes.Document

class PostParser {

    var title: String? = null
        private set
    var username: String? = null
        private set
    var imgUrlList: ArrayList<String> = ArrayList()
        private set
    var prevPageUrl: String? = null
        private set
    var nextPageUrl: String? = null
        private set
    var pageJumpUrl: String? = null
        private set
    var pageCount: Int = 1
        private set
    var replyUrl: String? = null
        private set
    var sector: String? = null
        private set
    var formhash: String? = null
        private set
    val replyList = ArrayList<MultiItemEntity>()

    constructor(document: Document) {
        title = document.select("span#thread_subject").first()?.ownText()
        prevPageUrl = document.select("div.pg a.prev").first()?.attr("href")
        nextPageUrl = document.select("div.pg a.nxt").first()?.attr("href")
        document.select("input[name=custompage]").first()?.attr("onkeydown")?.let {
            pageJumpUrl = "'(.*)'".toRegex().find(it)?.groupValues?.get(1)
        }
        val pageCountStr = document.select("div.pg a.last").first()?.ownText() ?: ""
        pageCount = "\\d+".toRegex().find(pageCountStr)?.value?.toIntOrNull() ?: 1

        val elemNavs = document.select("div#pt div.z a")
        if (elemNavs.size >= 2) {
            sector = elemNavs[elemNavs.size - 2].ownText()
        }

        document.select("form#fastpostform").first().run {
            replyUrl = attr("action")
            formhash = select("input[name=\"formhash\"]")?.first()?.attr("value")
        }
        document.select("div[style*=\"display: none\"]").remove()
        document.select("dl.tattl.attm dd p.mbn").remove()

        val elements = document.select("div#postlist > div[id^='post_']")
        elements.forEach {
            val elemAuthor = it.select("div.pls.favatar div.authi a.xw1").first()
            val author = elemAuthor.ownText()
            val authorUid = elemAuthor.attr("href").replace("[^\\d]+".toRegex(), "")
            val avatarUrl = it.select("div.avatar a.avtm img").first()?.attr("abs:src") ?: "https://bbs.yamibo.com/uc_server/images/noavatar_middle.gif"
            val floorNum = it.select("div.pi strong a em").first().ownText().toIntOrNull() ?: 0

            val content = it.select("td[id^='postmessage']").first()
            val appendix = it.select("div.pattl").first()
            appendix?.let { content.appendChild(appendix) }

            for (image in content.select("img")) {
                image.attr("src", image.attr("abs:src"))

                if (image.hasAttr("file")) {
                    if (image.attr("file").contains("bbs.yamibo.com/images/common")) {
                        image.remove()
                        continue
                    }
                    val imgUrl = UrlUtils.getFullUrl(image.attr("file"))
                    image.attr("src", imgUrl)
                    imgUrlList.add(imgUrl)
                }
            }
            val contentHTML = "<p style=\"word-break:break-all;\">" + content.html() + "</p>"
            val postDate = it.select("em[id^='authorposton']").text()
            replyList.add(Reply(author, avatarUrl, authorUid, contentHTML, postDate, floorNum))
            Log.d(ContentValues.TAG, "parsePost: $author / $avatarUrl / $postDate")
        }
    }
}