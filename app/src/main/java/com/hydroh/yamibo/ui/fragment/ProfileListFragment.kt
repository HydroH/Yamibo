package com.hydroh.yamibo.ui.fragment

import android.content.ContentValues
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.ProfileListAdapter
import com.hydroh.yamibo.util.parser.ProfileListParser
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document

class ProfileListFragment : Fragment() {

    private lateinit var mProfilePostList: List<MultiItemEntity>
    private var mNextPageUrl: String? = null
    private lateinit var mUrl: String

    private val mSwipeRefreshLayout by lazy { view!!.find<SwipeRefreshLayout>(R.id.refresh_common) }
    private val mHintText by lazy { view!!.find<TextView>(R.id.hint_text) }
    private val mLoadProgressBar by lazy { view!!.find<ProgressBar>(R.id.hint_progressbar) }
    private val mContentRecyclerView by lazy { view!!.find<RecyclerView>(R.id.list_common) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            mUrl = getString(Constants.ARG_INTENT_URL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_profile_list, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSwipeRefreshLayout.setOnRefreshListener {
            loadProfilePosts(mSwipeRefreshLayout)
        }
        mHintText.setOnClickListener {
            loadProfilePosts(it)
        }
        loadProfilePosts(mHintText)
    }

    private fun loadProfilePosts(view: View) {
        Log.d(ContentValues.TAG, "refreshNetwork: URL: $mUrl")

        if (view.id == R.id.hint_text) {
            mHintText.visibility = View.GONE
            mLoadProgressBar.visibility = View.VISIBLE
        } else if (view.id == R.id.refresh_common) {
            mSwipeRefreshLayout.isRefreshing = true
        }

        WebRequest.getHtmlDocument(mUrl, false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val profileListParser = ProfileListParser(document)
                mProfilePostList = profileListParser.profilePostList
                mNextPageUrl = profileListParser.nextPageUrl
                ctx.runOnUiThread {
                    mHintText.visibility = View.GONE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false

                    mContentRecyclerView.layoutManager = LinearLayoutManager(ctx)

                    mContentRecyclerView.adapter = ProfileListAdapter(mProfilePostList).apply {
                        setOnLoadMoreListener({
                            if (mNextPageUrl != null) {
                                WebRequest.getHtmlDocument(mNextPageUrl!!, false, ctx, object : DocumentCallbackListener {
                                    override fun onFinish(document: Document) {
                                        val profileListMoreParser = ProfileListParser(document)
                                        ctx.runOnUiThread {
                                            Log.d(ContentValues.TAG, "post: LoadMore Complete.")
                                            mNextPageUrl = profileListMoreParser.nextPageUrl
                                            addData(profileListMoreParser.profilePostList)
                                            loadMoreComplete()
                                        }
                                    }

                                    override fun onError(e: Exception) {
                                        ctx.runOnUiThread {
                                            Log.d(ContentValues.TAG, "post: LoadMore failed.")
                                            loadMoreFail()
                                        }
                                    }
                                })
                            } else {
                                ctx.runOnUiThread {
                                    Log.d(ContentValues.TAG, "post: LoadMore End.")
                                    loadMoreEnd()
                                }
                            }
                        }, mContentRecyclerView)
                    }

                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                ctx.runOnUiThread {
                    (mContentRecyclerView.adapter as ProfileListAdapter?)?.clear()
                    mHintText.visibility = View.VISIBLE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String) =
                ProfileListFragment().apply {
                    arguments = Bundle().apply {
                        putString(Constants.ARG_INTENT_URL, url)
                    }
                }
    }
}
