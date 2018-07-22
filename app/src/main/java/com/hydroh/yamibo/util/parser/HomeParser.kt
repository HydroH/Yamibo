package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.*
import org.jsoup.nodes.Document

class HomeParser {

    var isLoggedIn = false
        private set
    var title: String? = null
        private set
    var username: String? = null
        private set
    var avatarUrl: String? = null
        private set
    var uid: String? = null
        private set
    var nextPageUrl: String? = null
        private set
    var formhash: String? = null
        private set
    val groupList = ArrayList<MultiItemEntity>()

    constructor(document: Document, isProgressive: Boolean = false) {
        title = document.select("div.bm_h.cl h1.xs2 a").first()?.ownText()
        nextPageUrl = document.select("div.pg a.nxt").first()?.attr("href")
        avatarUrl = document.select("img.header-tu-img").first()?.attr("src")?.replace("small", "big")
        avatarUrl?.let {
            isLoggedIn = true
            document.select("ul#mycp1_menu").first()?.child(0)?.run {
                username = ownText()
                uid = attr("href").replace("[^\\d]+".toRegex(), "")
            }
        }
        formhash = document.select("form#scbar_form input[name=\"formhash\"]").first()?.attr("value")
        if (!isProgressive) {
            val tagList = ArrayList<PostTag>()
            var selectedPos = 0
            document.select("ul#thread_types li a").forEachIndexed { index, element ->
                element.run {
                    val title = ownText()
                    val url = attr("href")
                    val postNum = select("span.xg1.num").first()?.ownText()?.toIntOrNull() ?: 0
                    val selected = parent().hasClass("xw1")
                    if (selected) selectedPos = index
                    tagList.add(PostTag(title, url, postNum, selected))
                }
            }
            groupList.add(PostTagList(tagList, selectedPos))

            document.select("div.bm.bmw div.bm_h.cl").forEach {
                val title = it.select("h2").first().text()
                val group = SectorGroup(title)

                val elemSectors = it.nextElementSibling()
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
        val elemPosts = document.select("table#threadlisttableid tbody")
        elemPosts.first() ?: return
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
            val postTime = elemPost.select("td.by em span").first().ownText()
            val replyNum = elemPost.select("td.num a").first().ownText().toIntOrNull() ?: 0

            val post = Post(title, tag, author, postTime, replyNum, url, "", "")

            if (isProgressive) {
                if (elemID.startsWith("normalthread")) {
                    groupList.add(post)
                }
            } else {
                if (elemID.startsWith("stickthread")) {
                    groupStick.addSubItem(post)
                } else if (elemID.startsWith("normalthread")) {
                    groupNormal.addSubItem(post)
                }
            }
        }
        if (isProgressive) return
        if (groupStick.hasSubItem()) {
            groupList.add(groupStick)
        }
        if (groupNormal.hasSubItem()) {
            groupList.add(groupNormal)
        }
    }
}