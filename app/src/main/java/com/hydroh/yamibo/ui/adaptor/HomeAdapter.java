package com.hydroh.yamibo.ui.adaptor;


import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Sector;
import com.hydroh.yamibo.model.SectorGroup;
import com.hydroh.yamibo.ui.SectorActivity;

import java.util.List;

public class HomeAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private static final String TAG = HomeAdapter.class.getSimpleName();

    public static final int TYPE_GROUP = 0;
    public static final int TYPE_SECTOR = 1;

    @SuppressWarnings("unchecked")
    public HomeAdapter(List data) {
        super(data);
        addItemType(TYPE_GROUP, R.layout.item_group);
        addItemType(TYPE_SECTOR, R.layout.item_sector);
    }

    @Override
    protected void convert(final BaseViewHolder holder, final MultiItemEntity item) {
        switch (holder.getItemViewType()) {
            case TYPE_GROUP:
                final SectorGroup group = (SectorGroup) item;
                holder.setText(R.id.group_title, group.getTitle());
                if (group.isExpanded()) {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less_black_24dp);
                } else {
                    holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more_black_24dp);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        Log.d(TAG, "Sector group position " + position + " clicked.");
                        if (group.isExpanded()) {
                            collapse(position);
                            holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_more_black_24dp);
                        } else {
                            expand(position);
                            holder.setImageResource(R.id.group_expand, R.drawable.ic_expand_less_black_24dp);
                        }
                    }
                });
                break;

            case TYPE_SECTOR:
                final Sector sector = (Sector) item;
                holder.setText(R.id.sector_title, sector.getTitle());
                if (sector.getUnreadNum() > 0) {
                    holder.setText(R.id.sector_unread_num, Integer.toString(sector.getUnreadNum()));
                } else {
                    holder.setBackgroundRes(R.id.sector_unread_num, R.drawable.none);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        Log.d(TAG, "Sector position " + position + " clicked.");
                        Sector sector = (Sector)mData.get(position);
                        Intent intent = new Intent(mContext, SectorActivity.class);
                        intent.putExtra("url", sector.getUrl());
                        mContext.startActivity(intent);
                    }
                });
                break;
        }
    }
}
