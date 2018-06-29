package com.hydroh.yamibo.ui

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.widget.Toast

import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.adapter.ImageBrowserAdapter

class ImageGalleryActivity : Activity() {

    private lateinit var adapter: ImageBrowserAdapter
    private var position: Int = -1
    private var urlList: ArrayList<String> = ArrayList()

    private var toast: Toast? = null
    private val imageBrowserPager by lazy { findViewById<ViewPager>(R.id.image_viewpager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        position = intent.getIntExtra("imgPosition", -1)
        urlList = intent.getStringArrayListExtra("imgUrlList")

        adapter = ImageBrowserAdapter(this, urlList)
        imageBrowserPager.adapter = adapter
        showToast("${position + 1} / ${urlList.size}")

        imageBrowserPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                showToast("${position + 1} / ${urlList.size}")
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        imageBrowserPager.currentItem = position
    }

    public override fun onStop() {
        super.onStop()
        toast?.let { toast!!.cancel() }
    }

    private fun showToast(text: String) {
        toast?.let { toast!!.cancel() }
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        toast!!.show()
    }
}
