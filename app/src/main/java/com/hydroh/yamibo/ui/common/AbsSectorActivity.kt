package com.hydroh.yamibo.ui.common

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.internal.LinkedTreeMap
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.HomeActivity
import com.hydroh.yamibo.ui.LoginActivity
import com.hydroh.yamibo.ui.fragment.ARG_TITLE
import com.hydroh.yamibo.ui.fragment.ARG_URL
import com.hydroh.yamibo.ui.fragment.HomeFragment
import com.hydroh.yamibo.util.PrefUtils
import de.hdodenhof.circleimageview.CircleImageView

abstract class AbsSectorActivity(private val layoutResId: Int) : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, HomeFragment.InteractListener {

    private val mLayout by lazy { findViewById<DrawerLayout>(
            if (layoutResId == R.layout.activity_home) R.id.layout_home else R.id.layout_sector
    ) }
    private val mNavView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private lateinit var mNavHeaderAvatar: CircleImageView
    private lateinit var mNavHeaderUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        mNavView.run {
            menu.getItem(0).isChecked = layoutResId == R.layout.activity_home
            setNavigationItemSelectedListener(this@AbsSectorActivity)
            mNavHeaderAvatar = getHeaderView(0).findViewById(R.id.nav_header_avatar)
            mNavHeaderUsername = getHeaderView(0).findViewById(R.id.nav_header_username)
        }

        var url: String? = null
        var title: String? = null
        intent?.let {
            if (it.action == Intent.ACTION_VIEW) {
                val uri = it.data
                url = uri.path.removePrefix("/")
            } else {
                it.extras?.let {
                    url = it.getString(ARG_URL)
                    title = it.getString(ARG_TITLE)
                }
            }
        }
        fragmentManager.beginTransaction().add(R.id.layout_fragment, HomeFragment.newInstance(url, title)).commit()
    }

    override fun onBackPressed() {
        if (mLayout.isDrawerOpen(GravityCompat.START)) {
            mLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                if (layoutResId == R.layout.activity_home) {

                } else {
                    val intent = Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            R.id.nav_message -> { }
            R.id.nav_history -> { }
            R.id.nav_favorite -> { }
            R.id.nav_settings -> { }
            R.id.nav_logout -> {
                PrefUtils.setCookiePreference(this, LinkedTreeMap<String, String>())
                val intent = Intent("com.hydroh.yamibo.REFRESH")
                sendBroadcast(intent)
            }
        }
        mLayout.closeDrawers()
        return true
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onToolbarReady(toolbar: Toolbar) {
        val toggle = ActionBarDrawerToggle(
                this, mLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onHomeRefresh() {
        mLayout.closeDrawers()
    }

    override fun onUserStatReady(isLoggedIn: Boolean, avatarUrl: String?, username: String?) {
        if (isLoggedIn) {
            Glide.with(this).load(avatarUrl).crossFade().into(mNavHeaderAvatar)
            mNavHeaderUsername.text = username ?: mNavHeaderUsername.text
            mNavHeaderAvatar.setOnClickListener(null)
        } else {
            mNavHeaderAvatar.setImageResource(R.mipmap.ic_launcher_round)
            mNavHeaderUsername.setText(R.string.nav_header_username_hint)
            mNavHeaderAvatar.setOnClickListener {
                startLoginActivity()
            }
        }
    }
}
