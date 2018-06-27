package com.hydroh.yamibo.util;

import android.util.Log;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.model.Post;
import com.hydroh.yamibo.model.Reply;
import com.hydroh.yamibo.model.Sector;
import com.hydroh.yamibo.model.SectorGroup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DocumentParser {
    private Document doc;
    private boolean isMobile;
    private List<String> imgUrlList;

    public DocumentParser(Document doc, boolean isMobile) {
        this.doc = doc;
        this.isMobile = isMobile;
    }

    public List<MultiItemEntity> parseHome() {
        if (isMobile) {
            throw new RuntimeException("Incompatible viewport!");
        }
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

        Elements elemPosts = doc.select("table#threadlisttableid tbody");
        if (elemPosts.first() == null) {
            return groupList;
        }
        SectorGroup groupStick = new SectorGroup("置顶主题");
        SectorGroup groupNormal = new SectorGroup("版块主题");

        for (Element elemPost : elemPosts) {
            String elemID = elemPost.id();
            if (!elemID.contains("thread")) {
                continue;
            }
            Element elemTitle = elemPost.child(0).child(1).select("a.s.xst").first();
            String title = elemTitle.ownText();
            String url = elemTitle.attr("href");
            Element elemTag = elemPost.child(0).child(1).select("em a").first();
            String tag = "";
            if (elemTag != null) {
                tag = "[" + elemTag.ownText() + "]";
            }

            String author = elemPost.select("td.by cite a").first().ownText();
            String replyStr = elemPost.select("td.num a").first().ownText();
            int replyNum;
            try {
                replyNum = Integer.parseInt(replyStr);
            } catch (NumberFormatException e) {
                replyNum = 0;
            }
            Post post = new Post(title, tag, author, replyNum, url);

            if (elemID.startsWith("stickthread")) {
                groupStick.addSubItem(post);
            } else if (elemID.startsWith("normalthread")) {
                groupNormal.addSubItem(post);
            }
        }
        if (groupStick.hasSubItem()) {
            groupList.add(groupStick);
        }
        if (groupNormal.hasSubItem()) {
            groupList.add(groupNormal);
        }
        return groupList;
    }

    public List<MultiItemEntity> parsePost() {
        if (isMobile) {
            throw new RuntimeException("Incompatible viewport!");
        }
        List<MultiItemEntity> replyList = new ArrayList<>();
        imgUrlList = new ArrayList<>();
        doc.select("div[style*=\"display: none\"]").remove();
        doc.select("dl.tattl.attm dd p.mbn").remove();

        Elements elements = doc.select("div#postlist > div[id^='post_']");
        for (Element element : elements) {
            String author = element.select("div.pls.favatar div.authi a.xw1").text();
            String avatarUrl = element.select("div.avatar a.avtm img").attr("abs:src");
            int floorNum;
            try {
                floorNum = Integer.parseInt(element.select("div.pi strong a em").first().ownText());
            } catch (NumberFormatException e) {
                floorNum = 0;
            }
            Element content = element.select("td[id^='postmessage']").first();
            Element appendix = element.select("div.pattl").first();
            if (appendix != null) {
                content.appendChild(appendix);
            }
            for (Element image : content.select("img")) {
                image.attr("src", image.attr("abs:src"));

                if (image.hasAttr("file")) {
                    String imgUrl = (image.attr("file").startsWith("http") ? "" : HttpUtil.BASE_URL)
                            + image.attr("file");
                    image.attr("src", imgUrl);
                    imgUrlList.add(imgUrl);
                }
            }
            String contentHTML = "<p style=\"word-break:break-all;\">" + content.html() + "</p>";
            Log.d(TAG, "parsePost: " + contentHTML);
            String postDate = element.select("em[id^='authorposton']").text();
            replyList.add(new Reply(author, avatarUrl, contentHTML, postDate, floorNum));
            Log.d(TAG, "parsePost: " + author + avatarUrl + postDate);
            for (String item : imgUrlList) Log.d(TAG, "parsePost: " + item);
        }

        return replyList;
    }

    public List<String> getImgUrlList() { return imgUrlList; }
}
