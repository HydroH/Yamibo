package com.hydroh.yamibo.model;


import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.ui.adapter.PostAdapter;

public class Reply implements MultiItemEntity {
    private String author, avatarUrl, contentHTML;
    private String postDate;
    private int floorNum;

    public Reply(String author, String avatarUrl, String contentHTML, String postDate, int floorNum) {
        this.author = author;
        this.avatarUrl = avatarUrl;
        this.contentHTML = contentHTML;
        this.postDate = postDate;
        this.floorNum = floorNum;
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getContentHTML() {
        return contentHTML;
    }

    public String getPostDate() {
        return postDate;
    }

    public int getFloorNum() {
        return floorNum;
    }

    @Override
    public int getItemType() { return PostAdapter.TYPE_REPLY; }
}
