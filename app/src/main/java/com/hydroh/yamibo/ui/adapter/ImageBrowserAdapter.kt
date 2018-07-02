package com.hydroh.yamibo.ui.adapter

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.hydroh.yamibo.R
import java.lang.Exception

class ImageBrowserAdapter(private val context: Activity, private var imgUrlList: List<String>) : PagerAdapter() {

    override fun getCount(): Int {
        return imgUrlList.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {
        val view = View.inflate(context, R.layout.item_img_browser, null)
        val imageBrowserView = view.findViewById<ImageView>(R.id.image_browser_view)
        val progressBarLoading = view.findViewById<ProgressBar>(R.id.progressbar_image_loading)
        val textHintError = view.findViewById<TextView>(R.id.text_image_error)
        val imgUrl = imgUrlList[position]
        val photoViewAttacher = PhotoViewAttacher(imageBrowserView)

        Glide.with(context)
                .load(imgUrl)
                .crossFade()
                .into(object : GlideDrawableImageViewTarget(imageBrowserView) {
                    override fun onStart() {
                        super.onStart()
                        progressBarLoading.visibility = View.VISIBLE
                        textHintError.visibility =View.GONE
                    }
                    override fun onResourceReady(resource: GlideDrawable, animation: GlideAnimation<in GlideDrawable>?) {
                        super.onResourceReady(resource, animation)
                        progressBarLoading.visibility = View.GONE
                        textHintError.visibility =View.GONE
                        photoViewAttacher.update()
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        progressBarLoading.visibility = View.GONE
                        textHintError.visibility = View.VISIBLE
                    }
                })

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
