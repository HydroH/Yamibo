package com.hydroh.yamibo.ui

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.internal.LinkedTreeMap
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.HomeAdapter
import com.hydroh.yamibo.util.CookieUtil
import com.hydroh.yamibo.util.DocumentParser
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var homeItemList: List<MultiItemEntity>
    private var url: String = DEFAULT_URL
    private var nextPageUrl: String? = null

    private val swipeRefreshLayout by lazy { findViewById<SwipeRefreshLayout>(R.id.refresh_common) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_home) }
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.layout_home) }
    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private val navHeaderAvatar by lazy { findViewById<CircleImageView>(R.id.nav_header_avatar) }
    private val navHeaderUsername by lazy { findViewById<TextView>(R.id.nav_header_username) }
    private val hintTextView by lazy { findViewById<TextView>(R.id.hint_text) }
    private val hintProgressBar by lazy { findViewById<ProgressBar>(R.id.hint_progressbar) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.list_common) }

    private val refreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            drawerLayout.closeDrawers()
            swipeRefreshLayout.isRefreshing = true
            loadHome(swipeRefreshLayout)
        }
    }

    companion object {
        const val DEFAULT_URL = "forum.php"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        registerReceiver(refreshBroadcastReceiver, IntentFilter("com.hydroh.yamibo.REFRESH"))

        swipeRefreshLayout.setOnRefreshListener { loadHome(swipeRefreshLayout) }
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        toolbar.setOnClickListener {
            recyclerView.scrollToPosition(0)
        }

        hintTextView.setOnClickListener {
            loadHome(it)
        }

        val extras = intent?.extras
        extras?.let {
            url = extras.getString("url", DEFAULT_URL)
            title = extras.getString("title", "百合会")
        }

        Log.d(TAG, "onCreate: URL: $url")
        loadHome(hintTextView)
    }

    override fun onDestroy() {
        unregisterReceiver(refreshBroadcastReceiver)
        super.onDestroy()
    }

    private fun loadHome(view: View) {
        Log.d(TAG, "refreshNetwork: URL: $url")

        if (view.id == R.id.hint_text) {
            hintTextView.visibility = View.GONE
            hintProgressBar.visibility = View.VISIBLE
        }

        WebRequest.getHtmlDocument(url, false, this, object : DocumentCallbackListener {
            override fun onFinish(docParser: DocumentParser) {
                homeItemList = docParser.parseHome()
                nextPageUrl = docParser.nextPageUrl
                runOnUiThread {
                    hintTextView.visibility = View.GONE
                    hintProgressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    val layoutManager = LinearLayoutManager(recyclerView.context)
                    recyclerView.layoutManager = layoutManager

                    setupNavDrawer(docParser.isLoggedIn, docParser.avatarUrl, docParser.username)

                    val adapter = HomeAdapter(homeItemList)
                    recyclerView.adapter = adapter
                    if (!url.startsWith(DEFAULT_URL)) {
                        adapter.setOnLoadMoreListener({
                            if (nextPageUrl != null) {
                                WebRequest.getHtmlDocument(nextPageUrl!!, false, recyclerView.context, object : DocumentCallbackListener {
                                    override fun onFinish(docParser: DocumentParser) {
                                        val homeMoreSubItemList = docParser.parseHome(true)
                                        runOnUiThread {
                                            Log.d(TAG, "post: LoadMore Complete.")
                                            nextPageUrl = docParser.nextPageUrl
                                            adapter.addData(homeMoreSubItemList)
                                            adapter.loadMoreComplete()
                                        }
                                    }
                                    override fun onError(e: Exception) {
                                        runOnUiThread {
                                            Log.d(TAG, "post: LoadMore failed.")
                                            adapter.loadMoreFail()
                                        }
                                    }
                                })
                            } else {
                                runOnUiThread {
                                    Log.d(TAG, "post: LoadMore End.")
                                    adapter.loadMoreEnd()
                                }
                            }
                        }, recyclerView)
                    }
                    adapter.expandAll()
                    adapter.collapseSticky()

                    val dividerItemDecoration = DividerItemDecoration(
                            recyclerView.context,
                            layoutManager.orientation
                    )
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(recyclerView.context, R.drawable.divider_horizontal_thin)!!)
                    if (recyclerView.itemDecorationCount == 0) {
                        recyclerView.addItemDecoration(dividerItemDecoration)
                    }
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    val adapter = recyclerView.adapter as HomeAdapter?
                    adapter?.clear()
                    hintTextView.visibility = View.VISIBLE
                    hintProgressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_view -> { }
            R.id.nav_gallery -> { }
            R.id.nav_slideshow -> { }
            R.id.nav_manage -> { }
            R.id.nav_settings -> { }
            R.id.nav_logout -> {
                CookieUtil.setCookiePreference(this, LinkedTreeMap<String, String>())
                val intent = Intent("com.hydroh.yamibo.REFRESH")
                sendBroadcast(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupNavDrawer(isLoggedIn: Boolean, avatarUrl: String?, username: String?) {
        if (isLoggedIn) {
            this?.let { Glide.with(this).load(avatarUrl).crossFade().into(navHeaderAvatar) }
            navHeaderUsername?.text = username ?: navHeaderUsername.text
            navHeaderAvatar?.setOnClickListener(null)
        } else {
            navHeaderAvatar?.setImageResource(R.mipmap.ic_launcher_round)
            navHeaderUsername?.setText(R.string.nav_header_username_hint)
            navHeaderAvatar?.setOnClickListener {
                startLoginActivity(it)
            }
        }
    }

    private fun startLoginActivity(view: View) {
        val intent = Intent(view.context, LoginActivity::class.java)
        view.context.startActivity(intent)
    }
}
