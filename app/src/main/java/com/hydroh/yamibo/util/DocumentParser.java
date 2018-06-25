package com.hydroh.yamibo.util;

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
    public DocumentParser(Document doc) {
        this.doc = doc;
    }

    public List<SectorGroup> toGroupList() {
        List<SectorGroup> groupList = new ArrayList<>();

        Elements elemGroups = doc.select(".bm.bmw.fl");
        for (Element elemGroup : elemGroups) {
            String title = elemGroup.select("h2 a").first().ownText();

            SectorGroup group = new SectorGroup(title);
            for (Element elemSector : elemGroup.select("ul li a")) {
                String sectorTitle = elemSector.ownText();
                String url = elemSector.attr("href");
                int unreadNum;
                try {
                    unreadNum = Integer.parseInt(elemSector.child(0).ownText());
                } catch (NumberFormatException e) {
                    unreadNum = 0;
                }
                group.addSubItem(new Sector(sectorTitle, unreadNum, url));
            }
            groupList.add(group);
        }
        return groupList;
    }

    public List<Thread> toThreadList() {
        List<Thread> threadList = new ArrayList<>();

        Elements elemThreads = doc.select("div.threadlist ul li");
        for (Element elemThread : elemThreads) {
            String title = elemThread.select("a").first().ownText();
            String author = elemThread.select("a span.by").first().ownText();
            int replyNum;
            try {
                replyNum = Integer.parseInt(elemThread.select("span.num").first().ownText());
            } catch (NumberFormatException e) {
                replyNum = 0;
            }
            Element elemImgSpan = elemThread.select("span.icon_tu").first();
            boolean containsImage = elemImgSpan != null;

            threadList.add(new Thread(title, author, replyNum, containsImage));
        }
        return threadList;
    }

    public List<Post> toPostList() {
        return new ArrayList<>();
    }
}
