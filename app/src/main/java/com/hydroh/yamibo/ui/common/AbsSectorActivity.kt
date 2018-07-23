package com.hydroh.yamibo.ui.common

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.internal.LinkedTreeMap
import com.hydroh.yamibo.BuildConfig
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.JsonCallbackListener
import com.hydroh.yamibo.ui.HomeActivity
import com.hydroh.yamibo.ui.LoginActivity
import com.hydroh.yamibo.ui.ProfileActivity
import com.hydroh.yamibo.ui.fragment.HistoryFragment
import com.hydroh.yamibo.ui.fragment.HomeFragment
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.PrefUtils
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

abstract class AbsSectorActivity(private val layoutResId: Int) : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, HomeInteractListener {

    private var mPageUrl: String? = null
    private var mPageTitle: String? = null

    private val mLayout by lazy {
        findViewById<DrawerLayout>(
                if (layoutResId == R.layout.activity_home) R.id.layout_home else R.id.layout_sector
        )
    }
    private val mNavView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private lateinit var mNavHeaderAvatar: CircleImageView
    private lateinit var mNavHeaderUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        if (layoutResId == R.layout.activity_home && PrefUtils.getFirstLaunch(this)) {
            PrefUtils.setFirstLaunch(this, false)
            WebRequest.checkVersion(object : JsonCallbackListener {
                override fun onFinish(jsonObject: JSONObject) {
                    val build = jsonObject.getInt(Constants.ARG_JSON_BUILD)
                    val version = jsonObject.getString(Constants.ARG_JSON_VERSION)
                    val description = jsonObject.getString(Constants.ARG_JSON_DESC)
                    val url = jsonObject.getString(Constants.ARG_JSON_URL)
                    Log.d(ContentValues.TAG, "onFinish: Remote build: $build, Local build: ${BuildConfig.VERSION_CODE}")
                    if (build > BuildConfig.VERSION_CODE) {
                        val dialogBuilder = AlertDialog.Builder(this@AbsSectorActivity).apply {
                            setTitle("发现新版本")
                            setMessage("发现新版本：$version\n更新日志：\n$description")
                            setCancelable(true)
                            setPositiveButton("更新") { dialog, which ->
                                dialog.dismiss()
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                            setNegativeButton("取消") { dialog, which ->
                                dialog.dismiss()
                            }
                        }
                        runOnUiThread {
                            dialogBuilder.create().show()
                        }
                    }
                }

                override fun onError(e: Exception) {
                    e.printStackTrace()
                }
            })
        }

        mNavView.run {
            menu.getItem(0).isChecked = layoutResId == R.layout.activity_home
            setNavigationItemSelectedListener(this@AbsSectorActivity)
            mNavHeaderAvatar = getHeaderView(0).findViewById(R.id.nav_header_avatar)
            mNavHeaderUsername = getHeaderView(0).findViewById(R.id.nav_header_username)
        }

        intent?.let {
            if (it.action == Intent.ACTION_VIEW) {
                val uri = it.data
                mPageUrl = uri.path.removePrefix("/")
            } else {
                it.extras?.let {
                    mPageUrl = it.getString(Constants.ARG_INTENT_URL)
                    mPageTitle = it.getString(Constants.ARG_INTENT_TITLE)
                }
            }
        }
        switchFragment(HomeFragment::class.java.simpleName)
    }

    override fun onBackPressed() {
        when {
            mLayout.isDrawerOpen(GravityCompat.START) ->
                mLayout.closeDrawer(GravityCompat.START)
            supportFragmentManager.findFragmentByTag(HomeFragment::class.java.simpleName)?.isVisible != true ->
                switchFragment(HomeFragment::class.java.simpleName)
            else ->
                super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_sector -> {
                switchFragment(HomeFragment::class.java.simpleName)
            }
            R.id.nav_home -> {
                if (layoutResId == R.layout.activity_home) {
                    switchFragment(HomeFragment::class.java.simpleName)
                } else {
                    val intent = Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            R.id.nav_message -> {
            }
            R.id.nav_history -> {
                switchFragment(HistoryFragment::class.java.simpleName)
            }
            R.id.nav_favorite -> {
            }
            R.id.nav_settings -> {
            }
            R.id.nav_logout -> {
                PrefUtils.setCookiePreference(this, LinkedTreeMap<String, String>())
                val intent = Intent("com.hydroh.yamibo.REFRESH")
                sendBroadcast(intent)
            }
        }
        mLayout.closeDrawers()
        return true
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

    override fun onUserStatReady(isLoggedIn: Boolean, avatarUrl: String?, username: String?, uid: String?) {
        if (isLoggedIn) {
            mNavView.menu.clear()
            mNavView.inflateMenu(R.menu.nav_drawer_logged)
            mNavView.menu.run {
                if (layoutResId == R.layout.activity_home) {
                    findItem(R.id.nav_sector).isVisible = false
                    findItem(R.id.nav_home).isChecked = true
                } else {
                    findItem(R.id.nav_sector).isChecked = true
                }
            }
            Glide.with(this).load(avatarUrl).crossFade().into(mNavHeaderAvatar)
            mNavHeaderUsername.text = username ?: mNavHeaderUsername.text
            mNavHeaderAvatar.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra(Constants.ARG_INTENT_UID, uid)
                        .putExtra(Constants.ARG_INTENT_USERNAME, username)
                        .putExtra(Constants.ARG_INTENT_AVATAR_URL, avatarUrl)
                startActivity(intent)
            }
        } else {
            mNavView.menu.clear()
            mNavView.inflateMenu(R.menu.nav_drawer)
            mNavView.menu.run {
                if (layoutResId == R.layout.activity_home) {
                    findItem(R.id.nav_sector).isVisible = false
                    findItem(R.id.nav_home).isChecked = true
                } else {
                    findItem(R.id.nav_sector).isChecked = true
                }
            }
            mNavHeaderAvatar.setImageResource(R.mipmap.ic_launcher_round)
            mNavHeaderUsername.setText(R.string.nav_header_username_hint)
            mNavHeaderAvatar.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun switchFragment(tag: String) {
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach {
            if (it is HomeFragment) {
                transaction.hide(it)
            } else {
                transaction.remove(it)
            }
        }
        if (fragment != null) {
            transaction.show(fragment)
        } else {
            fragment = when (tag) {
                HomeFragment::class.java.simpleName -> {
                    HomeFragment.newInstance(mPageUrl, mPageTitle)
                }
                HistoryFragment::class.java.simpleName -> {
                    HistoryFragment()
                }
                else -> {
                    null
                }
            }
            transaction.add(R.id.layout_fragment, fragment, fragment::class.java.simpleName)
        }
        mNavView.menu.run {
            when (tag) {
                HomeFragment::class.java.simpleName -> {
                    if (layoutResId == R.layout.activity_home) {
                        findItem(R.id.nav_home).isChecked = true
                    } else {
                        findItem(R.id.nav_sector).isChecked = true
                    }
                }
                HistoryFragment::class.java.simpleName -> {
                    findItem(R.id.nav_history).isChecked = true
                }
            }
        }

        transaction.commit()
    }
}
