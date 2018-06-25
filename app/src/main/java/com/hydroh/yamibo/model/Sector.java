package com.hydroh.yamibo.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.ui.adaptor.HomeAdapter;

public class Sector implements MultiItemEntity {
    private String title;
    private int unreadNum;
    private String url;

    public Sector(String title, int unreadNum, String url) {
        this.title = title;
        this.unreadNum = unreadNum;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public int getUnreadNum() {
        return unreadNum;
    }

    public String getUrl() { return url; }

    @Override
    public int getItemType() {
        return HomeAdapter.TYPE_SECTOR;
    }
}

