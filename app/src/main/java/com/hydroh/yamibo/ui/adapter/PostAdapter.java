package com.hydroh.yamibo.ui.adapter;


import android.content.Intent;
import android.graphics.Color;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.text.Spanned;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Reply;
import com.hydroh.yamibo.ui.ImageGalleryActivity;
import com.zzhoujay.richtext.ImageHolder;
import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.callback.Callback;
import com.zzhoujay.richtext.callback.ImageFixCallback;
import com.zzhoujay.richtext.callback.OnImageClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private static final String TAG = PostAdapter.class.getSimpleName();

    public static final int TYPE_REPLY = 0;

    private List<String> imgUrlList;
    private HashMap<String, String> urlKeyMap;

    @SuppressWarnings("unchecked")
    public PostAdapter(List data, List<String> imgUrlList) {
        super(data);
        addItemType(TYPE_REPLY, R.layout.item_reply);
        this.imgUrlList = imgUrlList;
        urlKeyMap = new HashMap<>();
    }

    @Override
    protected void convert(final BaseViewHolder holder, final MultiItemEntity item) {
        switch (holder.getItemViewType()) {
            case TYPE_REPLY:
                final Reply reply = (Reply) item;
                holder.setText(R.id.reply_author, reply.getAuthor())
                        .setText(R.id.reply_date, reply.getPostDate())
                        .setText(R.id.reply_no, "#" + reply.getFloorNum());
                Glide.with(mContext).load(reply.getAvatarUrl()).transition(new DrawableTransitionOptions().crossFade())
                        .into((ImageView) holder.getView(R.id.reply_avatar));
                Log.d(TAG, "convert: Loading avatar: " + reply.getAvatarUrl());
                RichText.fromHtml(reply.getContentHTML())
                        .singleLoad(false)
                        .autoFix(true)
                        .resetSize(true)
                        .showBorder(false)
                        .borderColor(Color.argb(1, 1, 1, 1))
                        .borderSize(0)
                        .fix(new ImageFixCallback() {
                            @Override
                            public void onInit(ImageHolder holder) {
                                if (!urlKeyMap.containsKey(holder.getSource())) {
                                    urlKeyMap.put(holder.getSource(), holder.getKey());
                                }
                            }
                            @Override
                            public void onLoading(ImageHolder holder) {
                            }
                            @Override
                            public void onSizeReady(ImageHolder holder, int imageWidth, int imageHeight, ImageHolder.SizeHolder sizeHolder) {
                            }
                            @Override
                            public void onImageReady(ImageHolder holder, int width, int height) {
                                if (!imgUrlList.contains(holder.getSource())) {
                                    holder.setWidth(width * 2);
                                }
                            }
                            @Override
                            public void onFailure(ImageHolder holder, Exception e) {
                            }
                        })
                        .imageClick(new OnImageClickListener() {
                            @Override
                            public void imageClicked(List<String> imageUrls, int position) {
                                String imgUrl = imageUrls.get(position);
                                if (imgUrlList.contains(imgUrl)) {
                                    Log.d(TAG, "openImage: " + imgUrl);
                                    Intent intent = new Intent();
                                    intent.putExtra("imgUrl", imgUrl);
                                    intent.putStringArrayListExtra("imgUrlList", (ArrayList<String>) imgUrlList);
                                    intent.putExtra("urlKeyMap", urlKeyMap);
                                    intent.setClass(mContext, ImageGalleryActivity.class);
                                    mContext.startActivity(intent);
                                }
                            }
                        })
                        .into((TextView) holder.getView(R.id.reply_content));
                break;
        }
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

}
