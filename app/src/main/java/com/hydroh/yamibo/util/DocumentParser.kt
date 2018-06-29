package com.hydroh.yamibo.util

import android.content.ContentValues.TAG
import android.util.Log
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.Reply
import com.hydroh.yamibo.model.Sector
import com.hydroh.yamibo.model.SectorGroup
import com.hydroh.yamibo.network.WebRequest
import org.jsoup.nodes.Document

class DocumentParser(private val doc: Document, private val isMobile: Boolean) {
    internal var imgUrlList: ArrayList<String> = ArrayList()
        private set
    internal var nextPageUrl: String? = null
        private set

    fun parseHome(isProgressive: Boolean = false): List<MultiItemEntity> {
        if (isMobile) {
            throw RuntimeException("Incompatible viewport!")
        }
        nextPageUrl = doc.select("div.pg a.nxt").first()?.attr("href")

        val groupList = ArrayList<MultiItemEntity>()
        if (!isProgressive) {
            val elemGroupHeads = doc.select("div.bm.bmw div.bm_h.cl")
            for (elemGroupHead in elemGroupHeads) {
                val title = elemGroupHead.select("h2").first().text()
                val group = SectorGroup(title)

                val elemSectors = elemGroupHead.nextElementSibling()
                for (elemSector in elemSectors.select("div.bm_c tr")) {
                    if (elemSector.children().size >= 2) {
                        val elemSectorMain = elemSector.child(1)
                        val sectorTitle = elemSectorMain.select("h2 a").first().ownText()
                        val sectorUrl = elemSectorMain.select("h2 a").first().attr("href")
                        val sectorUnreadNum = elemSectorMain.select("h2 em").first()?.ownText()
                                ?.replace("[^\\d]".toRegex(), "")?.toIntOrNull() ?: 0
                        val sectorDescription = elemSectorMain.select("p.xg2").first()?.ownText()
                                ?: sectorTitle
                        group.addSubItem(Sector(sectorTitle, sectorUnreadNum, sectorDescription, sectorUrl))
                    }
                }
                groupList.add(group)
            }
        }

        val elemPosts = doc.select("table#threadlisttableid tbody")
        elemPosts.first() ?: if (!isProgressive) return groupList
        val groupStick = SectorGroup("置顶主题")
        val groupNormal = SectorGroup("版块主题")

        for (elemPost in elemPosts) {
            val elemID = elemPost.id()
            if (!elemID.contains("thread")) {
                continue
            }
            val elemTitle = elemPost.child(0).child(1).select("a.s.xst").first()
            val title = elemTitle.ownText()
            val url = elemTitle.attr("href")
            val elemTag = elemPost.child(0).child(1).select("em a").first()
            var tag = ""
            elemTag?.let { tag = "[${elemTag.ownText()}]" }

            val author = elemPost.select("td.by cite a").first().ownText()
            val replyNum = elemPost.select("td.num a").first().ownText().toIntOrNull() ?: 0

            val post = Post(title, tag, author, replyNum, url)

            if (!isProgressive && elemID.startsWith("stickthread")) {
                groupStick.addSubItem(post)
            } else if (elemID.startsWith("normalthread")) {
                groupNormal.addSubItem(post)
            }
        }
        if (isProgressive) return groupNormal.subItems
        if (groupStick.hasSubItem()) {
            groupList.add(groupStick)
        }
        if (groupNormal.hasSubItem()) {
            groupList.add(groupNormal)
        }
        return groupList
    }

    fun parsePost(): List<MultiItemEntity> {
        if (isMobile) {
            throw RuntimeException("Incompatible viewport!")
        }
        nextPageUrl = doc.select("div.pg a.nxt").first()?.attr("href")

        val replyList = ArrayList<MultiItemEntity>()
        doc.select("div[style*=\"display: none\"]").remove()
        doc.select("dl.tattl.attm dd p.mbn").remove()

        val elements = doc.select("div#postlist > div[id^='post_']")
        for (element in elements) {
            val author = element.select("div.pls.favatar div.authi a.xw1").text()
            val avatarUrl = element.select("div.avatar a.avtm img").attr("abs:src")
            val floorNum = element.select("div.pi strong a em").first().ownText().toIntOrNull() ?: 0

            val content = element.select("td[id^='postmessage']").first()
            val appendix = element.select("div.pattl").first()
            appendix?.let { content.appendChild(appendix) }

            for (image in content.select("img")) {
                image.attr("src", image.attr("abs:src"))

                if (image.hasAttr("file")) {
                    val imgUrl = (if (image.attr("file").startsWith("http")) "" else WebRequest.BASE_URL) + image.attr("file")
                    image.attr("src", imgUrl)
                    imgUrlList.add(imgUrl)
                }
            }
            val contentHTML = "<p style=\"word-break:break-all;\">" + content.html() + "</p>"
            val postDate = element.select("em[id^='authorposton']").text()
            replyList.add(Reply(author, avatarUrl, contentHTML, postDate, floorNum))
            Log.d(TAG, "parsePost: $author / $avatarUrl / $postDate")
        }

        return replyList
    }
}
