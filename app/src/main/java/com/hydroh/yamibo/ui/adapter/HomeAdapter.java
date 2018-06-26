package com.hydroh.yamibo.ui.adapter;


import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Thread;
import com.hydroh.yamibo.model.Sector;
import com.hydroh.yamibo.model.SectorGroup;
import com.hydroh.yamibo.ui.HomeActivity;

import java.util.List;

public class HomeAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private static final String TAG = HomeAdapter.class.getSimpleName();

    public static final int TYPE_GROUP = 0;
    public static final int TYPE_SECTOR = 1;
    public static final int TYPE_THREAD = 2;

    @SuppressWarnings("unchecked")
    public HomeAdapter(List data) {
        super(data);
        addItemType(TYPE_GROUP, R.layout.item_group);
        addItemType(TYPE_SECTOR, R.layout.item_sector);
        addItemType(TYPE_THREAD, R.layout.item_thread);
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
                holder.setText(R.id.sector_description, sector.getDescription());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        Log.d(TAG, "Sector position " + position + " clicked.");
                        Sector sector = (Sector) mData.get(position);
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        intent.putExtra("url", sector.getUrl());
                        mContext.startActivity(intent);
                    }
                });
                break;

            case TYPE_THREAD:
                final Thread thread = (Thread) item;
                Spanned spanned = Html.fromHtml("<font color='#0099cc'>" + thread.getTag() +"</font>" + thread.getTitle());
                holder.setText(R.id.thread_title, spanned)
                        .setText(R.id.thread_author, thread.getAuthor())
                        .setText(R.id.thread_replyNum, Integer.toString(thread.getReplyNum()));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = holder.getAdapterPosition();
                        Log.d(TAG, "Thread position " + position + " clicked.");
                        Thread thread = (Thread) mData.get(position);
                        Toast.makeText(mContext, thread.getUrl(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
}
