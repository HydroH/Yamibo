package com.hydroh.yamibo.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.adapter.PostAdapter
import com.hydroh.yamibo.util.DocumentParser
import com.hydroh.yamibo.util.HttpCallbackListener
import com.hydroh.yamibo.util.HttpUtil
import com.zzhoujay.richtext.RichText

class PostActivity : AppCompatActivity() {

    private lateinit var replyList: List<MultiItemEntity>
    private lateinit var imgUrlList: ArrayList<String>
    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val sectionRefresh = findViewById<SwipeRefreshLayout>(R.id.refresh_common)
        sectionRefresh.setOnRefreshListener { loadPosts(findViewById(R.id.refresh_common)) }

        val extras = intent?.extras
        extras?.let {
            url = extras.getString("url", "")
            title = extras.getString("title", "百合会")
        }
        Log.d(TAG, "onCreate: URL: $url")

        RichText.initCacheDir(this)
        loadPosts(findViewById(R.id.hint_text))
    }

    private fun loadPosts(view: View) {
        Log.d(TAG, "refreshNetwork: URL: $url")

        if (view.id == R.id.hint_text) {
            val hintText = findViewById<TextView>(R.id.hint_text)
            hintText.visibility = View.GONE
            val hintProgressBar = findViewById<ProgressBar>(R.id.hint_progressbar)
            hintProgressBar.visibility = View.VISIBLE
        }

        HttpUtil.getHtmlDocument(url, false, object : HttpCallbackListener {
            override fun onFinish(docParser: DocumentParser) {
                replyList = docParser.parsePost()
                imgUrlList = docParser.imgUrlList
                runOnUiThread {
                    val hintProgressBar = findViewById<ProgressBar>(R.id.hint_progressbar)
                    hintProgressBar.visibility = View.GONE
                    val recyclerView = findViewById<RecyclerView>(R.id.list_common)
                    val sectionRefresh = findViewById<SwipeRefreshLayout>(R.id.refresh_common)
                    sectionRefresh.isRefreshing = false
                    val layoutManager = LinearLayoutManager(recyclerView.context)
                    recyclerView.layoutManager = layoutManager

                    val adapter = PostAdapter(replyList, imgUrlList)
                    recyclerView.adapter = adapter
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    val recyclerView = findViewById<RecyclerView>(R.id.list_common)
                    val adapter = recyclerView.adapter as PostAdapter
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

}
