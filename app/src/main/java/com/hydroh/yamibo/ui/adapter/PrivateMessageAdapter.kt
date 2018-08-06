package com.hydroh.yamibo.ui.adapter

import android.graphics.Color
import android.util.Log
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.common.TextDrawable
import com.hydroh.yamibo.model.PrivateMessage
import com.hydroh.yamibo.ui.ProfileActivity
import com.hydroh.yamibo.ui.common.AbsMultiAdapter
import com.hydroh.yamibo.ui.common.ItemType.TYPE_PM_ME
import com.hydroh.yamibo.ui.common.ItemType.TYPE_PM_OTHER
import com.zzhoujay.richtext.CacheType
import com.zzhoujay.richtext.ImageHolder
import com.zzhoujay.richtext.RichText
import com.zzhoujay.richtext.callback.ImageFixCallback
import com.zzhoujay.richtext.ig.GlideImageGetter
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.startActivity

class PrivateMessageAdapter(data: List<MultiItemEntity>) : AbsMultiAdapter(data) {

    companion object {
        private val TAG = this::class.java.simpleName
    }

    init {
        addItemType(TYPE_PM_ME, R.layout.item_speech_me)
        addItemType(TYPE_PM_OTHER, R.layout.item_speech_other)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when (holder.itemViewType) {
            TYPE_PM_ME -> {
                (item as PrivateMessage).run {
                    Glide.with(mContext).load(authorAvatarUrl).crossFade()
                            .into(holder.getView(R.id.speech_me_avatar))
                    RichText.fromHtml(contentHtml)
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
                            .placeHolder { imageHolder, config, textView ->
                                val hintText = ""
                                val width = 50
                                val height = 50
                                imageHolder.width = width
                                imageHolder.height = height
                                val textDrawable = TextDrawable(textView.resources, hintText, Color.GRAY, Color.LTGRAY)
                                textDrawable.setBounds(0, 0, width, height)
                                textDrawable
                            }
                            .errorImage { imageHolder, config, textView ->
                                val hintText = ""
                                val width = 50
                                val height = 50
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
                                    holder.width = width * 2
                                    holder.height = height * 2
                                }

                                override fun onFailure(holder: ImageHolder, e: Exception?) {
                                    Log.d(TAG, "onFailure: ${holder.source}")
                                    GlideImageGetter.removeCache(holder.source)
                                }
                            })
                            .into(holder.getView(R.id.speech_me_content))
                }
            }
            TYPE_PM_OTHER -> {
                (item as PrivateMessage).run {
                    holder.setText(R.id.speech_other_name, author)
                    Glide.with(mContext).load(authorAvatarUrl).crossFade()
                            .into(holder.getView(R.id.speech_other_avatar))
                    holder.getView<CircleImageView>(R.id.speech_other_avatar).setOnClickListener {
                        mContext.startActivity<ProfileActivity>(
                                Constants.ARG_INTENT_UID to authorUid,
                                Constants.ARG_INTENT_USERNAME to author,
                                Constants.ARG_INTENT_AVATAR_URL to authorAvatarUrl
                        )
                    }

                    RichText.fromHtml(contentHtml)
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
                            .placeHolder { imageHolder, config, textView ->
                                val hintText = ""
                                val width = 50
                                val height = 50
                                imageHolder.width = width
                                imageHolder.height = height
                                val textDrawable = TextDrawable(textView.resources, hintText, Color.GRAY, Color.LTGRAY)
                                textDrawable.setBounds(0, 0, width, height)
                                textDrawable
                            }
                            .errorImage { imageHolder, config, textView ->
                                val hintText = ""
                                val width = 50
                                val height = 50
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
                                    holder.width = width * 2
                                    holder.height = height * 2
                                }

                                override fun onFailure(holder: ImageHolder, e: Exception?) {
                                    Log.d(TAG, "onFailure: ${holder.source}")
                                    GlideImageGetter.removeCache(holder.source)
                                }
                            })
                            .into(holder.getView(R.id.speech_other_content))
                }
            }
        }
    }
}