package com.hydroh.yamibo.ui.adaptor;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Section;
import com.hydroh.yamibo.ui.HomeActivity;

import java.util.List;

import static android.content.ContentValues.TAG;

public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Section> mSectionList;

    static class SectorViewHolder extends RecyclerView.ViewHolder {
        View sectionView;
        TextView sectorTitle;
        TextView sectorDescription;

        public SectorViewHolder(View view) {
            super(view);
            sectionView = view;
            sectorTitle = (TextView) view.findViewById(R.id.sector_title);
            sectorDescription = (TextView) view.findViewById(R.id.sector_description);
        }
    }

    static class ThreadViewHolder extends RecyclerView.ViewHolder {
        View sectionView;
        TextView threadTitle;
        TextView threadAuthor;

        public ThreadViewHolder(View view) {
            super(view);
            sectionView = view;
            threadTitle = (TextView) view.findViewById(R.id.thread_title);
            threadAuthor = (TextView) view.findViewById(R.id.thread_author);
        }
    }

    public SectionAdapter(List<Section> sectionList) {
        mSectionList = sectionList;
    }

    @Override
    public int getItemViewType(int position) {
        Section section = mSectionList.get(position);
        Log.d(TAG, "getItemViewType: " + section.getSectionType());
        return section.getSectionType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view;
        final RecyclerView.ViewHolder holder;
        switch (viewType) {
            case Section.TYPE_SECTOR:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.sector_item, parent, false);
                holder = new SectorViewHolder(view);
                ((SectorViewHolder) holder).sectionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        Section section = mSectionList.get(position);
                        Intent intent = new Intent(parent.getContext(), HomeActivity.class);
                        intent.putExtra("url", section.getUrl());
                        intent.putExtra("title", section.getTitle());
                        parent.getContext().startActivity(intent);
                    }
                });
                break;

            case Section.TYPE_THREAD:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.thread_item, parent, false);
                holder = new ThreadViewHolder(view);
                ((ThreadViewHolder) holder).sectionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        Section section = mSectionList.get(position);
                        Intent intent = new Intent(parent.getContext(), HomeActivity.class);
                        intent.putExtra("url", section.getUrl());
                        intent.putExtra("title", "贴子详情");
                        parent.getContext().startActivity(intent);
                    }
                });
                break;

            default:
                holder = null;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Section section = mSectionList.get(position);
        switch (section.getSectionType()) {
            case Section.TYPE_SECTOR:
                ((SectorViewHolder) holder).sectorTitle.setText(section.getTitle() + " " + section.getNewPosts());
                ((SectorViewHolder) holder).sectorDescription.setText(section.getDescription());
                break;
            case Section.TYPE_THREAD:
                ((ThreadViewHolder) holder).threadTitle.setText(section.getTitle());
                ((ThreadViewHolder) holder).threadAuthor.setText(section.getAuthor());
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mSectionList.size();
    }
}
