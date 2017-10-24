package com.hydroh.yamibo.util;

import android.util.Log;

import com.hydroh.yamibo.model.Section;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ObjectUtil {
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
            String author = element.select("td.by cite a").text();
            String postDate = element.select("tr:last-child").select("em a").text();
            int replies = Integer.parseInt(element.select("td.num a").text());
            int views = Integer.parseInt(element.select("td.num em").text());
            sectionList.add(new Section(title, url, category, author, postDate, replies, views));
            Log.d(TAG, "documentToSections: Loaded: " + category + title + author + postDate + replies + views);
            Log.d(TAG, "documentToSections: Loaded URL: " + url);
        }

        return sectionList;
    }
}
