package com.hydroh.yamibo.util;

import android.util.Log;

import com.hydroh.yamibo.model.Post;
import com.hydroh.yamibo.model.Section;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ObjectUtil {
    public static final String BASE_URL = "http://bbs.yamibo.com/";
    public static List<String> imgUrlList;

    public static List<Section> documentToSections(Document doc) {
        List<Section> sectionList = new ArrayList<Section>();

        Elements elements = doc.select("tbody tr td h2 a");
        for (Element element : elements) {
            String title = element.text();
            String url = element.attr("abs:href");
            String desc = element.parent().parent().select("p").text();
            String newPosts = element.parent().select("em").text();
            sectionList.add(new Section(title, url, desc, newPosts));
            Log.d(TAG, "documentToSections: Loaded title: " + title + newPosts);
            Log.d(TAG, "documentToSections: Loaded desc: " + desc);
            Log.d(TAG, "documentToSections: Loaded url: " + url);
        }

        elements = doc.select("tbody[id^='normalthread']");
        for (Element element : elements) {
            String title = element.select("a.s.xst").text();
            String url = element.select("a.s.xst").attr("abs:href");
            String category = element.select("tr th em a").text();
            String author = element.select("td.by cite a").first().text();
            String postDate = element.select("tr:last-child").select("em a").text();
            int replies = Integer.parseInt(element.select("td.num a").text());
            int views = Integer.parseInt(element.select("td.num em").text());
            sectionList.add(new Section(title, url, category, author, postDate, replies, views));
            Log.d(TAG, "documentToSections: Loaded: " + category + title + author + postDate + replies + views);
            Log.d(TAG, "documentToSections: Loaded URL: " + url);
        }

        return sectionList;
    }

    public static List<Post> documentToPosts(Document doc) {
        List<Post> postList = new ArrayList<>();
        imgUrlList = new ArrayList<>();

        Elements elements = doc.select("div#postlist > div[id^='post_']");
        for (Element element : elements) {
            String author = element.select("div.pls.favatar div.authi a.xw1").text();
            String avatar = element.select("div.avatar a.avtm img").attr("abs:src");
            Element content = element.select("td[id^='postmessage']").first();
            for (Element image : content.select("img")) {
                image.attr("src", image.attr("abs:src"));
                image.removeAttr("onmouseover");
                image.removeAttr("initialized");
            }
            for (Element image : content.select("img.zoom")) {
                String imgUrl = (image.attr("file").startsWith("http") ? "" : BASE_URL)
                        + image.attr("file");
                image.attr("src", imgUrl);
                image.attr("style", "max-width: 100% !important; height:auto;");

                image.attr("onclick", "window.imageListener.openImage(this.src);");
                imgUrlList.add(imgUrl);
            }
            String contentHTML = "<p style=\"word-break:break-all;\">" + content.html() + "</p>";
            String postDate = element.select("em[id^='authorposton']").text();
            postList.add(new Post(author, avatar, contentHTML, postDate));
            Log.d(TAG, "documentToPosts: " + author + avatar + postDate);
            Log.d(TAG, "documentToPosts: HTML: " + contentHTML);
            for (String item : imgUrlList) Log.d(TAG, "documentToPosts: " + item);
        }

        return postList;
    }
}
