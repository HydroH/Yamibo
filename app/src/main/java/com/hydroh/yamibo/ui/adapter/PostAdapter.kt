package com.hydroh.yamibo.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.model.Reply
import com.hydroh.yamibo.ui.ImageGalleryActivity
import com.zzhoujay.richtext.CacheType
import com.zzhoujay.richtext.ImageHolder
import com.zzhoujay.richtext.RichText
import com.zzhoujay.richtext.callback.ImageFixCallback
import com.zzhoujay.richtext.ig.GlideImageGetter
import java.util.*

class PostAdapter(data: List<MultiItemEntity>, private var imgUrlList: ArrayList<String>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    companion object {
        private val TAG = PostAdapter::class.java.simpleName

        const val TYPE_REPLY = 0
    }

    init {
        addItemType(TYPE_REPLY, R.layout.item_reply)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_REPLY -> {
                val reply = item as Reply
                holder.setText(R.id.reply_author, reply.author)
                        .setText(R.id.reply_date, reply.postDate)
                        .setText(R.id.reply_no, "#" + reply.floorNum)
                Glide.with(mContext).load(reply.avatarUrl).crossFade()
                        .into(holder.getView(R.id.reply_avatar))
                Log.d(TAG, "convert: Loading avatar: ${reply.avatarUrl}")

                holder.getView<TextView>(R.id.reply_content).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                RichText.fromHtml(reply.contentHTML)
                        .imageGetter(GlideImageGetter())
                        .cache(CacheType.layout)
                        .singleLoad(false)
                        .autoFix(false)
                        .resetSize(true)
                        .autoPlay(true)
                        .scaleType(ImageHolder.ScaleType.fit_auto)
                        .showBorder(false)
                        .borderColor(Color.argb(1, 1, 1, 1))
                        .borderSize(0f)
                        .fix(object : ImageFixCallback {
                            override fun onInit(holder: ImageHolder) {}
                            override fun onLoading(holder: ImageHolder) {}
                            override fun onSizeReady(holder: ImageHolder, imageWidth: Int, imageHeight: Int, sizeHolder: ImageHolder.SizeHolder) {}
                            override fun onImageReady(holder: ImageHolder, width: Int, height: Int) {
                                if (imgUrlList.contains(holder.source)) {
                                    holder.isAutoFix = true
                                } else {
                                    holder.width = width * 2
                                }
                            }
                            override fun onFailure(holder: ImageHolder, e: Exception?) {
                                Log.d(TAG, "onFailure: ${holder.source}")
                                if (imgUrlList.contains(holder.source)) {
                                    holder.isAutoFix = true
                                } else {
                                    holder.width = 50
                                    holder.height = 50
                                }
                            }
                        })
                        .imageClick { imageUrls, position ->
                            val imgUrl = imageUrls[position]
                            val imgPosition = imgUrlList.indexOf(imgUrl)
                            if (imgPosition >= 0) {
                                Log.d(TAG, "openImage: $imgUrl")
                                val intent = Intent()
                                intent.putExtra("imgPosition", imgPosition)
                                intent.putStringArrayListExtra("imgUrlList", imgUrlList)
                                intent.setClass(mContext, ImageGalleryActivity::class.java)
                                mContext.startActivity(intent)
                            }
                        }
                        .into(holder.getView(R.id.reply_content))
            }
        }
    }

    fun clear() {
        mData.clear()
        notifyDataSetChanged()
    }
}
