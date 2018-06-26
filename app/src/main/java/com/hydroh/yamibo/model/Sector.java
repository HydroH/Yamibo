package com.hydroh.yamibo.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.ui.adapter.HomeAdapter;

public class Sector implements MultiItemEntity {
    private String title;
    private int unreadNum;
    private String description;
    private String url;

    public Sector(String title, int unreadNum, String description, String url) {
        this.title = title;
        this.unreadNum = unreadNum;
        this.description = description;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public int getUnreadNum() {
        return unreadNum;
    }

    public String getDescription() { return description; }

    public String getUrl() { return url; }

    @Override
    public int getItemType() {
        return HomeAdapter.TYPE_SECTOR;
    }
}

