package com.hydroh.yamibo.ui.adapter;


import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Reply;
import com.hydroh.yamibo.ui.ImageGalleryActivity;
import com.zzhoujay.richtext.CacheType;
import com.zzhoujay.richtext.ImageHolder;
import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.callback.ImageFixCallback;
import com.zzhoujay.richtext.callback.OnImageClickListener;
import com.zzhoujay.richtext.ig.DefaultImageGetter;
import com.zzhoujay.richtext.ig.GlideImageGetter;

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
                holder.setText(R.id.reply_author, reply.getAuthor())
                        .setText(R.id.reply_date, reply.getPostDate())
                        .setText(R.id.reply_no, "#" + reply.getFloorNum());
                Glide.with(mContext).load(reply.getAvatarUrl()).crossFade()
                        .into((ImageView) holder.getView(R.id.reply_avatar));
                Log.d(TAG, "convert: Loading avatar: " + reply.getAvatarUrl());
                RichText.fromHtml(reply.getContentHTML())
                        .imageGetter(new GlideImageGetter())
                        .cache(CacheType.layout)
                        .singleLoad(false)
                        .autoFix(false)
                        .resetSize(true)
                        .autoPlay(true)
                        .scaleType(ImageHolder.ScaleType.fit_auto)
                        .showBorder(false)
                        .borderColor(Color.argb(1, 1, 1, 1))
                        .borderSize(0)
                        .fix(new ImageFixCallback() {
                            @Override
                            public void onInit(ImageHolder holder) {}
                            @Override
                            public void onLoading(ImageHolder holder) {}
                            @Override
                            public void onSizeReady(ImageHolder holder, int imageWidth, int imageHeight, ImageHolder.SizeHolder sizeHolder) {}
                            @Override
                            public void onImageReady(ImageHolder holder, int width, int height) {
                                if (imgUrlList.contains(holder.getSource())) {
                                    holder.setAutoFix(true);
                                } else {
                                    holder.setWidth(width * 2);
                                }
                            }
                            @Override
                            public void onFailure(ImageHolder holder, Exception e) {}
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
