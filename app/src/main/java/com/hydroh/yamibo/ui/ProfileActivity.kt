package com.hydroh.yamibo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.ui.adapter.ProfileFragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var mUid: String
    private lateinit var mUsername: String
    private lateinit var mAvatarUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar_profile)

        intent!!.extras!!.run {
            mUid = getString(Constants.ARG_INTENT_UID)
            mUsername = getString(Constants.ARG_INTENT_USERNAME)
            mAvatarUrl = getString(Constants.ARG_INTENT_AVATAR_URL)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = mUsername

        Glide.with(this).load(mAvatarUrl).crossFade().into(profile_avatar)
        profile_avatar.setOnClickListener {
            startActivity<ImageGalleryActivity>(
                    Constants.ARG_INTENT_IMG_POS to 0,
                    Constants.ARG_INTENT_IMG_URL_LIST to listOf(mAvatarUrl)
            )
        }
        pager_tab_profile.adapter = ProfileFragmentPagerAdapter(supportFragmentManager, mUid, tab_profile.tabCount, ctx)
        tab_profile.setupWithViewPager(pager_tab_profile)
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
}
