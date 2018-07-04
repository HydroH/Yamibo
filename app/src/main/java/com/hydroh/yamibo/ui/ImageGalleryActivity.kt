package com.hydroh.yamibo.ui

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import com.anthonycr.grant.PermissionsManager
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.adapter.ImageBrowserAdapter
import com.hydroh.yamibo.ui.view.HackyViewPager

class ImageGalleryActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName

    private lateinit var adapter: ImageBrowserAdapter
    private var position: Int = -1
    private var urlList: ArrayList<String> = ArrayList()

    private val imageBrowserPager by lazy { findViewById<HackyViewPager>(R.id.image_viewpager) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_image_browser) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        position = intent.getIntExtra("imgPosition", -1)
        urlList = intent.getStringArrayListExtra("imgUrlList")

        adapter = ImageBrowserAdapter(this, urlList)
        imageBrowserPager.adapter = adapter
        title = "${position + 1} / ${urlList.size}"

        imageBrowserPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                title = "${position + 1} / ${urlList.size}"
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
        imageBrowserPager.currentItem = position
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
        val animation = toolbar.animate()
        animation.interpolator = DecelerateInterpolator()
        animation.duration = 350
        if (toolbar.translationY >= 0) {
            animation.translationY(-toolbar.height.toFloat())
            // TODO: Toggle status bar
        } else {
            // TODO: Toggle status bar
            animation.translationY(0f)
        }
        animation.start()
    }
}
