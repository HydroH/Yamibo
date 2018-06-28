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
import com.hydroh.yamibo.util.CookieUtil
import com.hydroh.yamibo.util.DocumentParser
import com.hydroh.yamibo.util.HttpCallbackListener
import com.hydroh.yamibo.util.HttpUtil

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var homeItemList: List<MultiItemEntity>
    private var url: String = DEFAULT_URL

    companion object {
        const val DEFAULT_URL = "forum.php"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sectionRefresh = findViewById<SwipeRefreshLayout>(R.id.refresh_common)
        sectionRefresh.setOnRefreshListener { loadHome(findViewById(R.id.refresh_common)) }

        val toolbar = findViewById<Toolbar>(R.id.nav_toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val extras = intent?.extras
        extras?.let {
            url = extras.getString("url", DEFAULT_URL)
            title = extras.getString("title", "百合会")
        }
        CookieUtil.instance.getCookiePreference(this)

        Log.d(TAG, "onCreate: URL: $url")
        loadHome(findViewById(R.id.hint_text))
    }

    private fun loadHome(view: View) {
        Log.d(TAG, "refreshNetwork: URL: $url")

        if (view.id == R.id.hint_text) {
            val hintText = findViewById<TextView>(R.id.hint_text)
            hintText.visibility = View.GONE
            val hintProgressBar = findViewById<ProgressBar>(R.id.hint_progressbar)
            hintProgressBar.visibility = View.VISIBLE
        }

        HttpUtil.getHtmlDocument(url, false, object : HttpCallbackListener {
            override fun onFinish(docParser: DocumentParser) {
                homeItemList = docParser.parseHome()
                runOnUiThread {
                    val hintProgressBar = findViewById<ProgressBar>(R.id.hint_progressbar)
                    hintProgressBar.visibility = View.GONE
                    val recyclerView = findViewById<RecyclerView>(R.id.list_common)
                    val sectionRefresh = findViewById<SwipeRefreshLayout>(R.id.refresh_common)
                    sectionRefresh.isRefreshing = false
                    val layoutManager = LinearLayoutManager(recyclerView.context)
                    recyclerView.layoutManager = layoutManager

                    val adapter = HomeAdapter(homeItemList)
                    recyclerView.adapter = adapter
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
                    val recyclerView = findViewById<RecyclerView>(R.id.list_common)
                    val adapter = recyclerView.adapter as HomeAdapter
                    adapter.clear()
                    val hintText = findViewById<TextView>(R.id.hint_text)
                    hintText.visibility = View.VISIBLE
                    val hintProgressBar = findViewById<ProgressBar>(R.id.hint_progressbar)
                    hintProgressBar.visibility = View.GONE
                    val sectionRefresh = findViewById<SwipeRefreshLayout>(R.id.refresh_common)
                    sectionRefresh.isRefreshing = false
                }
            }
        })
    }

    fun startLoginActivity(view: View) {
        val intent = Intent(view.context, LoginActivity::class.java)
        view.context.startActivity(intent)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
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

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
