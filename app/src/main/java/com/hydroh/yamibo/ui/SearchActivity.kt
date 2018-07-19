package com.hydroh.yamibo.ui

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.SearchResultAdapter
import com.hydroh.yamibo.util.parser.SearchResultParser
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.list_common.*
import org.jsoup.nodes.Document

class SearchActivity : AppCompatActivity() {

    private lateinit var mSearchResultList: List<MultiItemEntity>
    private lateinit var mSearchRange: String
    private lateinit var mFid: String
    private lateinit var mFormHash: String
    private var mNextPageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(toolbar_search)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        refresh_common.isEnabled = false
        hint_text.setOnClickListener {
            loadResult(search_common.query.toString())
        }

        intent!!.extras!!.run {
            mSearchRange = getString(Constants.ARG_INTENT_NAME, "全部贴子")
            mFid = getString(Constants.ARG_INTENT_FID, "all")
            mFormHash = getString(Constants.ARG_INTENT_FORMHASH)
        }

        search_common.queryHint = "在${mSearchRange}中搜索…"
        val searchIcon = search_common.findViewById<ImageView>(android.support.v7.appcompat.R.id.search_mag_icon)
        searchIcon.run {
            (parent as ViewGroup).removeView(this)
        }
        search_common.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search_common.clearFocus()
                return loadResult(query)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadResult(query: String?): Boolean {
        if (query == null) return true

        val searchResultAdapter = list_common.adapter as SearchResultAdapter?
        searchResultAdapter?.clear()
        hint_text.visibility = View.GONE
        hint_progressbar.visibility = View.VISIBLE

        WebRequest.getSearchResult(query, mFid, mFormHash, this@SearchActivity, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val searchResultParser = SearchResultParser(document)
                mSearchResultList = searchResultParser.postList
                mNextPageUrl = searchResultParser.nextPageUrl

                runOnUiThread {
                    hint_progressbar.visibility = View.GONE

                    val layoutManager = LinearLayoutManager(this@SearchActivity)
                    list_common.layoutManager = layoutManager

                    val adapter = SearchResultAdapter(mSearchResultList)
                    list_common.adapter = adapter
                    adapter.setOnLoadMoreListener({
                        if (mNextPageUrl != null) {
                            WebRequest.getHtmlDocument(mNextPageUrl!!, false, list_common.context, object : DocumentCallbackListener {
                                override fun onFinish(document: Document) {
                                    val searchMoreParser = SearchResultParser(document)
                                    list_common.post {
                                        Log.d(ContentValues.TAG, "post: LoadMore Complete.")
                                        mNextPageUrl = searchMoreParser.nextPageUrl
                                        adapter.addData(searchMoreParser.postList)
                                        adapter.loadMoreComplete()
                                    }
                                }
                                override fun onError(e: Exception) {
                                    list_common.post {
                                        Log.d(ContentValues.TAG, "post: LoadMore failed.")
                                        adapter.loadMoreFail()
                                    }
                                }
                            })
                        } else {
                            list_common.post {
                                Log.d(ContentValues.TAG, "post: LoadMore End.")
                                adapter.loadMoreEnd()
                            }
                        }
                    }, list_common)
                }

            }

            override fun onError(e: Exception) {
                hint_text.visibility = View.VISIBLE
                hint_progressbar.visibility = View.GONE
            }
        })
        return true
    }
}
