package com.hydroh.yamibo.ui.adaptor;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Post;
import com.hydroh.yamibo.util.JavascriptInterface;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Post> mPostList;
    private List<String> imgUrlList;

    static class PostViewHolder extends RecyclerView.ViewHolder {
        View postView;
        TextView postAuthor;
        WebView postContent;

        public PostViewHolder(View view) {
            super(view);
            postView = view;
            postAuthor = (TextView) view.findViewById(R.id.post_author);
            postContent = (WebView) view.findViewById(R.id.post_content);
        }
    }

    public PostAdapter(List<Post> postList) {
        mPostList = postList;
        imgUrlList = new ArrayList<>();
    }

    public PostAdapter(List<Post> postList, List<String> imgUrlList) {
        mPostList = postList;
        this.imgUrlList = imgUrlList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        final RecyclerView.ViewHolder holder = new PostViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Post post = mPostList.get(position);
        ((PostViewHolder) holder).postAuthor.setText(post.getAuthor());
        WebView webView = ((PostViewHolder) holder).postContent;
        webView.loadDataWithBaseURL(null,
                post.getContentHTML(), "text/html", "utf-8", null);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavascriptInterface(webView.getContext(), imgUrlList), "imageListener");
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }
}
