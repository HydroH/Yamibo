package com.hydroh.yamibo.ui

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import com.anthonycr.grant.PermissionsManager
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.ui.adapter.ImageBrowserAdapter
import kotlinx.android.synthetic.main.activity_image_gallery.*

class ImageGalleryActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName

    private lateinit var adapter: ImageBrowserAdapter
    private var position: Int = -1
    private var urlList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)
        setSupportActionBar(toolbar_image_browser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        position = intent.getIntExtra(Constants.ARG_INTENT_IMG_POS, -1)
        urlList = intent.getStringArrayListExtra(Constants.ARG_INTENT_IMG_URL_LIST)

        adapter = ImageBrowserAdapter(this, urlList)
        image_viewpager.adapter = adapter
        title = "${position + 1} / ${urlList.size}"

        image_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                title = "${position + 1} / ${urlList.size}"
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
        image_viewpager.currentItem = position
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults)
    }

    fun toggleToolbar() {
        val animation = toolbar_image_browser.animate()
        animation.interpolator = DecelerateInterpolator()
        animation.duration = 350
        if (toolbar_image_browser.translationY >= 0) {
            animation.translationY(-toolbar_image_browser.height.toFloat())
            // TODO: Toggle status bar
        } else {
            // TODO: Toggle status bar
            animation.translationY(0f)
        }
        animation.start()
    }
}
