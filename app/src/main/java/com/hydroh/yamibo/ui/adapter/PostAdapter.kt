package com.hydroh.yamibo.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.TextDrawable
import com.hydroh.yamibo.model.Reply
import com.hydroh.yamibo.ui.ImageGalleryActivity
import com.hydroh.yamibo.ui.ProfileActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_REPLY
import com.zzhoujay.richtext.CacheType
import com.zzhoujay.richtext.ImageHolder
import com.zzhoujay.richtext.RichText
import com.zzhoujay.richtext.callback.ImageFixCallback
import com.zzhoujay.richtext.ig.GlideImageGetter
import java.util.*

class PostAdapter(data: List<MultiItemEntity>, private var imgUrlList: ArrayList<String>) : AbsMultiAdapter(data) {

    companion object {
        private val TAG = this::class.java.simpleName
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
                holder.getView<ImageView>(R.id.reply_avatar).setOnClickListener {
                    val intent = Intent(mContext, ProfileActivity::class.java)
                            .putExtra("uid", reply.authorUid)
                            .putExtra("username", reply.author)
                            .putExtra("avatarUrl", reply.avatarUrl)
                    mContext.startActivity(intent)
                }
                Log.d(TAG, "convert: Loading avatar: ${reply.avatarUrl}")

                RichText.fromHtml(reply.contentHTML)
                        .imageGetter(GlideImageGetter())
                        .cache(CacheType.all)
                        .singleLoad(false)
                        .autoFix(false)
                        .resetSize(true)
                        .autoPlay(false)
                        .scaleType(ImageHolder.ScaleType.fit_auto)
                        .showBorder(false)
                        .borderColor(Color.argb(1, 1, 1, 1))
                        .borderSize(0f)
                        .placeHolder{ imageHolder, config, textView ->
                            var hintText =  ""
                            var width = 50
                            var height = 50
                            if (imgUrlList.contains(imageHolder.source)) {
                                hintText = mContext.getString(R.string.image_loading_hint)
                                width = textView.width
                                height = width / 2
                            }
                            imageHolder.width = width
                            imageHolder.height = height
                            val textDrawable = TextDrawable(textView.resources, hintText, Color.GRAY, Color.LTGRAY)
                            textDrawable.setBounds(0, 0, width, height)
                            textDrawable
                        }
                        .errorImage{ imageHolder, config, textView ->
                            var hintText =  ""
                            var width = 50
                            var height = 50
                            if (imgUrlList.contains(imageHolder.source)) {
                                hintText = mContext.getString(R.string.image_load_fail_hint)
                                width = textView.width
                                height = width / 2
                            }
                            imageHolder.width = width
                            imageHolder.height = height
                            val textDrawable = TextDrawable(textView.resources, hintText, Color.GRAY, Color.LTGRAY)
                            textDrawable.setBounds(0, 0, width, height)
                            textDrawable
                        }
                        .fix(object : ImageFixCallback {
                            override fun onInit(holder: ImageHolder) {}
                            override fun onLoading(holder: ImageHolder) {}
                            override fun onSizeReady(holder: ImageHolder, imageWidth: Int, imageHeight: Int, sizeHolder: ImageHolder.SizeHolder) {}
                            override fun onImageReady(holder: ImageHolder, width: Int, height: Int) {
                                if (width > 100 && imgUrlList.contains(holder.source)) {
                                    holder.isAutoFix = true
                                } else {
                                    if (imgUrlList.contains(holder.source)) {
                                        imgUrlList.remove(holder.source)
                                    }
                                    holder.width = width * 2
                                }
                            }
                            override fun onFailure(holder: ImageHolder, e: Exception?) {
                                Log.d(TAG, "onFailure: ${holder.source}")
                                GlideImageGetter.removeCache(holder.source)
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
}
