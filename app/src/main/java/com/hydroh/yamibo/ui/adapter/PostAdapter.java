package com.hydroh.yamibo.ui.adapter;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Reply;
import com.hydroh.yamibo.util.JavascriptInterface;

import org.xml.sax.XMLReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private static final String TAG = PostAdapter.class.getSimpleName();

    public static final int TYPE_REPLY = 0;

    private List<String> imgUrlList;
    @SuppressWarnings("unchecked")
    public PostAdapter(List data, List<String> imgUrlList) {
        super(data);
        addItemType(TYPE_REPLY, R.layout.item_reply);
        this.imgUrlList = imgUrlList;
    }

    @Override
    protected void convert(final BaseViewHolder holder, final MultiItemEntity item) {
        switch (holder.getItemViewType()) {
            case TYPE_REPLY:
                final Reply reply = (Reply) item;
                Html.ImageGetter imageGetter = new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        Drawable drawable;
                        URL url;
                        try {
                            url = new URL(source);
                            drawable = Drawable.createFromStream(url.openStream(), "");
                        } catch (Exception e) {
                            return null;
                        }
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        return drawable;
                    }
                };
                Html.TagHandler tagHandler = new Html.TagHandler() {
                    @Override
                    public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {
                    }
                };

                Drawable avatar;
                try {
                    URL avatarUrl = new URL(reply.getAvatarUrl());
                    avatar = Drawable.createFromStream(avatarUrl.openStream(), "");
                } catch (Exception e) {
                    avatar = null;
                }

                holder.setText(R.id.reply_author, reply.getAuthor())
                        .setText(R.id.reply_date, reply.getPostDate())
                        .setText(R.id.reply_no, "#" + reply.getFloorNum());
                Glide.with(mContext).load(reply.getAvatarUrl()).crossFade().into((ImageView) holder.getView(R.id.reply_avatar));
                Log.d(TAG, "convert: Loading avatar: " + reply.getAvatarUrl());
                WebView replyContent = holder.getView(R.id.reply_content);
                replyContent.loadDataWithBaseURL(null, reply.getContentHTML(), "text/html", "utf-8", null);
                replyContent.setVerticalScrollBarEnabled(false);
                replyContent.setHorizontalScrollBarEnabled(false);
                replyContent.setScrollContainer(false);
                replyContent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                replyContent.getSettings().setJavaScriptEnabled(true);
                replyContent.addJavascriptInterface(new JavascriptInterface(replyContent.getContext(), imgUrlList), "imageListener");
                break;
        }
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

}
