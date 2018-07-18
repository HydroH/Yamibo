package com.hydroh.yamibo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.adapter.ProfileFragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val mUid by lazy { intent!!.extras!!.getString("uid") }
    private val mUsername by lazy { intent!!.extras!!.getString("username") }
    private val mAvatarUrl by lazy { intent!!.extras!!.getString("avatarUrl") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = mUsername

        Glide.with(this).load(mAvatarUrl).crossFade().into(profile_avatar)
        pager_tab_profile.adapter = ProfileFragmentPagerAdapter(supportFragmentManager, mUid, tab_profile.tabCount, this)
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
