package com.hydroh.yamibo.ui.fragment

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.ProfileListAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshListFragment
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.util.parser.ProfileListParser
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document

class ProfileListFragment : AbsRefreshListFragment() {

    private lateinit var mProfilePostList: List<MultiItemEntity>
    private var mNextPageUrl: String? = null
    private lateinit var mUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            mUrl = getString(Constants.ARG_INTENT_URL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_profile_list, container, false)

    override fun loadContent() {
        WebRequest.getHtmlDocument(mUrl, false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val profileListParser = ProfileListParser(document)
                mProfilePostList = profileListParser.profilePostList
                mNextPageUrl = profileListParser.nextPageUrl
                ctx.runOnUiThread {
                    setRefreshState(RefreshState.FINISH)

                    list_common.layoutManager = LinearLayoutManager(ctx)
                    list_common.adapter = ProfileListAdapter(mProfilePostList).apply {
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
                        }, list_common)
                    }

                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                ctx.runOnUiThread {
                    (list_common.adapter as ProfileListAdapter?)?.clear()
                    setRefreshState(RefreshState.ERROR)
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
