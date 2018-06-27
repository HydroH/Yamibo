package com.hydroh.yamibo.ui.adapter;


import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Post;
import com.hydroh.yamibo.model.Sector;
import com.hydroh.yamibo.model.SectorGroup;
import com.hydroh.yamibo.ui.HomeActivity;
import com.hydroh.yamibo.ui.PostActivity;

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
        addItemType(TYPE_THREAD, R.layout.item_post);
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
                        intent.putExtra("title", sector.getTitle());
                        mContext.startActivity(intent);
                    }
                });
                break;

            case TYPE_THREAD:
                final Post post = (Post) item;
                Spanned spanned = Html.fromHtml("<font color='#0099cc'>" + post.getTag() +"</font>" + post.getTitle());
                holder.setText(R.id.post_title, spanned)
                        .setText(R.id.post_author, post.getAuthor())
                        .setText(R.id.post_replyNum, Integer.toString(post.getReplyNum()));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = holder.getAdapterPosition();
                        Log.d(TAG, "Post position " + position + " clicked.");
                        Post post = (Post) mData.get(position);
                        Intent intent = new Intent(mContext, PostActivity.class);
                        intent.putExtra("url", post.getUrl());
                        intent.putExtra("title", post.getTitle());
                        mContext.startActivity(intent);
                    }
                });
        }
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public void collapseSticky() {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i) instanceof SectorGroup && ((SectorGroup) mData.get(i)).getTitle().contains("置顶")) {
                collapse(i);
                break;
            }
        }

    }
}
