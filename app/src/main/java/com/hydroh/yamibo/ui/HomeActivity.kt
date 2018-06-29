package com.hydroh.yamibo.ui

import android.content.ContentValues.TAG
import android.content.Intent
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
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.adapter.HomeAdapter
import com.hydroh.yamibo.ui.view.ToggledLoadMoreView
import com.hydroh.yamibo.util.CookieUtil
import com.hydroh.yamibo.util.DocumentParser
import com.hydroh.yamibo.util.HttpCallbackListener
import com.hydroh.yamibo.util.HttpUtil

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var homeItemList: List<MultiItemEntity>
    private var url: String = DEFAULT_URL
    private var nextPageUrl: String? = null

    private val swipeRefreshLayout by lazy { findViewById<SwipeRefreshLayout>(R.id.refresh_common) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_home) }
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.layout_home) }
    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private val hintTextView by lazy { findViewById<TextView>(R.id.hint_text) }
    private val hintProgressBar by lazy { findViewById<ProgressBar>(R.id.hint_progressbar) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.list_common) }

    companion object {
        const val DEFAULT_URL = "forum.php"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        swipeRefreshLayout.setOnRefreshListener { loadHome(swipeRefreshLayout) }
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        val extras = intent?.extras
        extras?.let {
            url = extras.getString("url", DEFAULT_URL)
            title = extras.getString("title", "百合会")
        }
        CookieUtil.instance.getCookiePreference(this)

        Log.d(TAG, "onCreate: URL: $url")
        loadHome(hintTextView)
    }

    private fun loadHome(view: View) {
        Log.d(TAG, "refreshNetwork: URL: $url")

        if (view.id == R.id.hint_text) {
            hintTextView.visibility = View.GONE
            hintProgressBar.visibility = View.VISIBLE
        }

        HttpUtil.getHtmlDocument(url, false, object : HttpCallbackListener {
            override fun onFinish(docParser: DocumentParser) {
                homeItemList = docParser.parseHome()
                nextPageUrl = docParser.nextPageUrl
                runOnUiThread {
                    hintProgressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    val layoutManager = LinearLayoutManager(recyclerView.context)
                    recyclerView.layoutManager = layoutManager

                    val adapter = HomeAdapter(homeItemList)
                    recyclerView.adapter = adapter
                    adapter.setLoadMoreView(ToggledLoadMoreView(!url.startsWith(DEFAULT_URL)))
                    adapter.setOnLoadMoreListener({
                        if (nextPageUrl != null) {
                            HttpUtil.getHtmlDocument(nextPageUrl!!, false, object : HttpCallbackListener {
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
                    val adapter = recyclerView.adapter as HomeAdapter
                    adapter.clear()
                    hintTextView.visibility = View.VISIBLE
                    hintProgressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    fun startLoginActivity(view: View) {
        val intent = Intent(view.context, LoginActivity::class.java)
        view.context.startActivity(intent)
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
