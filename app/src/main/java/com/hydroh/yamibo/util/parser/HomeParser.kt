package com.hydroh.yamibo.util.parser

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.Sector
import com.hydroh.yamibo.model.SectorGroup
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
    var nextPageUrl: String? = null
        private set
    val groupList = ArrayList<MultiItemEntity>()

    constructor(document: Document, isProgressive: Boolean = false) {
        title = document.select("div.bm_h.cl h1.xs2 a").first()?.ownText()
        nextPageUrl = document.select("div.pg a.nxt").first()?.attr("href")
        avatarUrl = document.select("img.header-tu-img").first()?.attr("src")?.replace("small", "big")
        avatarUrl?.let {
            isLoggedIn = true
            username = document.select("ul#mycp1_menu").first()?.child(0)?.ownText()
        }
        if (!isProgressive) {
            val elemGroupHeads = document.select("div.bm.bmw div.bm_h.cl")
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
        val elemPosts = document.select("table#threadlisttableid tbody")
        elemPosts.first() ?: if (!isProgressive) return
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

            val post = Post(title, tag, author, postTime, replyNum, url)

            if (!isProgressive && elemID.startsWith("stickthread")) {
                groupStick.addSubItem(post)
            } else if (elemID.startsWith("normalthread")) {
                groupNormal.addSubItem(post)
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