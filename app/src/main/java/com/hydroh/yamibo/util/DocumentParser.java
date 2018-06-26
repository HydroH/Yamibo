package com.hydroh.yamibo.util;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.model.Post;
import com.hydroh.yamibo.model.Sector;
import com.hydroh.yamibo.model.SectorGroup;
import com.hydroh.yamibo.model.Thread;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class DocumentParser {
    private Document doc;
    private boolean isMobile;

    public DocumentParser(Document doc, boolean isMobile) {
        this.doc = doc;
        this.isMobile = isMobile;
    }

    public List<MultiItemEntity> toHomeList() {
        List<MultiItemEntity> groupList = new ArrayList<>();
        Elements elemGroupHeads = doc.select("div.bm.bmw div.bm_h.cl");
        for (Element elemGroupHead : elemGroupHeads) {
            String title = elemGroupHead.select("h2").first().text();
            SectorGroup group = new SectorGroup(title);

            Element elemSectors = elemGroupHead.nextElementSibling();
            for (Element elemSector : elemSectors.select("div.bm_c tr")) {
                if (elemSector.children().size() >= 2) {
                    Element elemSectorMain = elemSector.child(1);
                    String sectorTitle = elemSectorMain.select("h2 a").first().ownText();
                    String sectorUrl = elemSectorMain.select("h2 a").first().attr("href");
                    int sectorUnreadNum = 0;
                    if (elemSectorMain.select("h2 em").first() != null) {
                        String sectorUnreadStr = elemSectorMain.select("h2 em").first().ownText().replaceAll("[^\\d]", "");
                        sectorUnreadNum = Integer.parseInt(sectorUnreadStr);
                    }
                    String sectorDescription = sectorTitle;
                    if (elemSectorMain.select("p.xg2").first() != null) {
                        sectorDescription = elemSectorMain.select("p.xg2").first().ownText();
                    }
                    group.addSubItem(new Sector(sectorTitle, sectorUnreadNum, sectorDescription, sectorUrl));
                }
            }
            groupList.add(group);
        }

        Elements elemThreads = doc.select("table#threadlisttableid tbody");
        if (elemThreads.first() == null) {
            return groupList;
        }
        SectorGroup groupStick = new SectorGroup("置顶主题");
        SectorGroup groupThread = new SectorGroup("版块主题");

        for (Element elemThread : elemThreads) {
            String elemID = elemThread.id();
            if (!elemID.contains("thread")) {
                continue;
            }
            Element elemTitle = elemThread.child(0).child(1).select("a.s.xst").first();
            String title = elemTitle.ownText();
            String url = elemTitle.attr("href");
            Element elemTag = elemThread.child(0).child(1).select("em a").first();
            String tag = "";
            if (elemTag != null) {
                tag = "[" + elemTag.ownText() + "]";
            }

            String author = elemThread.select("td.by cite a").first().ownText();
            String replyStr = elemThread.select("td.num a").first().ownText();
            int replyNum;
            try {
                replyNum = Integer.parseInt(replyStr);
            } catch (NumberFormatException e) {
                replyNum = 0;
            }
            Thread thread = new Thread(title, tag, author, replyNum, url);

            if (elemID.startsWith("stickthread")) {
                groupStick.addSubItem(thread);
            } else if (elemID.startsWith("normalthread")) {
                groupThread.addSubItem(thread);
            }
        }
        if (groupStick.hasSubItem()) {
            groupList.add(groupStick);
        }
        if (groupThread.hasSubItem()) {
            groupList.add(groupThread);
        }
        return groupList;
    }

    public List<Thread> toThreadList() {
        return new ArrayList<>();
    }

    public List<Post> toPostList() {
        return new ArrayList<>();
    }
}
