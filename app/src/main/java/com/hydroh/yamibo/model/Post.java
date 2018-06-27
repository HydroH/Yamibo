package com.hydroh.yamibo.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.ui.adapter.HomeAdapter;

public class Post implements MultiItemEntity {
    private String title;
    private String tag;
    private String author;
    private int replyNum;
    private String url;

    public Post(String title, String tag, String author, int replyNum, String url) {
        this.title = title;
        this.tag = tag;
        this.author = author;
        this.replyNum = replyNum;
        this.url = url;
    }

    public String getTitle() { return title; }
    public String getTag() { return tag; }
    public String getAuthor() { return author; }
    public int getReplyNum() { return replyNum; }
    public String getUrl() { return url; }

    @Override
    public int getItemType() {
        return HomeAdapter.TYPE_THREAD;
    }
}
