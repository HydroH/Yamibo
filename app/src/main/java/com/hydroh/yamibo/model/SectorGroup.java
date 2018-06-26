package com.hydroh.yamibo.model;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.ui.adapter.HomeAdapter;

public class SectorGroup extends AbstractExpandableItem<MultiItemEntity> implements MultiItemEntity {
    private String title;

    public SectorGroup(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getItemType() {
        return HomeAdapter.TYPE_GROUP;
    }

    @Override
    public int getLevel() {
        return 0;
    }
}
