package com.hydroh.yamibo.ui.adapter

import android.Manifest
import android.app.Activity
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.anthonycr.grant.PermissionsManager
import com.anthonycr.grant.PermissionsResultAction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.github.chrisbanes.photoview.PhotoView
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.hydroh.yamibo.R
import com.hydroh.yamibo.io.SaveImageTask
import com.hydroh.yamibo.ui.ImageGalleryActivity
import java.io.File

class ImageBrowserAdapter(private val context: Activity, private var imgUrlList: List<String>) : PagerAdapter() {
    companion object {
        private val TAG = this::class.java.simpleName
    }

    override fun getCount(): Int {
        return imgUrlList.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {
        val view = View.inflate(context, R.layout.item_img_browser, null)
        val imageBrowserView = view.findViewById<PhotoView>(R.id.image_browser_view)
        val progressBarLoading = view.findViewById<ProgressBar>(R.id.progressbar_image_loading)
        val textHintError = view.findViewById<TextView>(R.id.text_image_error)
        val imgUrl = imgUrlList[position]
        val photoViewAttacher = PhotoViewAttacher(imageBrowserView)

        photoViewAttacher.setOnClickListener {
            if (it.context is ImageGalleryActivity) {
                val activity = it.context as ImageGalleryActivity
                activity.toggleToolbar()
            } else {
                Log.d(TAG, "instantiateItem: Failed to toggle toolbar.")
            }
        }

        Glide.with(context)
                .load(imgUrl)
                .crossFade()
                .into(object : GlideDrawableImageViewTarget(imageBrowserView) {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        super.onLoadStarted(placeholder)
                        progressBarLoading.visibility = View.VISIBLE
                        textHintError.visibility = View.GONE
                        photoViewAttacher.setOnLongClickListener(null)
                        Log.d(TAG, "onLoadStarted: Image started loading.")
                    }

                    override fun onResourceReady(resource: GlideDrawable, animation: GlideAnimation<in GlideDrawable>?) {
                        super.onResourceReady(resource, animation)
                        progressBarLoading.visibility = View.GONE
                        textHintError.visibility =View.GONE
                        imageBrowserView.isLongClickable = true
                        photoViewAttacher.setOnLongClickListener {
                            Log.d(TAG, "onLongClick: Long click, showing context menu...")
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle(R.string.image_op_menu_title)
                            builder.setItems(R.array.image_op_menu_options) { dialogInterface, which ->
                                when (which) {
                                    0 -> {
                                        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(context,
                                                Array(1) { _ -> Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                object : PermissionsResultAction() {
                                                    override fun onGranted() {
                                                        val drawable = imageBrowserView.drawable
                                                        val saveImageTask = SaveImageTask(object : SaveImageTask.Callback {
                                                            override fun onSaveComplete(filePath: File) {
                                                                MediaScannerConnection.scanFile(context,
                                                                        Array<String>(1) { _ -> filePath.absolutePath },
                                                                        null,
                                                                        object : MediaScannerConnection.MediaScannerConnectionClient {
                                                                            override fun onMediaScannerConnected() {}
                                                                            override fun onScanCompleted(p0: String?, p1: Uri?) {}
                                                                        })
                                                                Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show()
                                                            }

                                                            override fun onError(e: Exception) {
                                                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                                            }
                                                        })
                                                        saveImageTask.execute(drawable)
                                                    }

                                                    override fun onDenied(permission: String?) {
                                                        Toast.makeText(context, "图片保存失败，请开启存储空间权限", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                    }
                                    1 -> TODO("Image share function")
                                }
                            }
                            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            builder.show()
                            true
                        }
                        Log.d(TAG, "onResourceReady: Image resource ready.")
                        photoViewAttacher.update()
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        progressBarLoading.visibility = View.GONE
                        textHintError.visibility = View.VISIBLE
                        imageBrowserView.isLongClickable = false
                        photoViewAttacher.setOnLongClickListener(null)
                        Log.d(TAG, "onLoadFailed: Image resource failed to load.")
                    }
                })

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
