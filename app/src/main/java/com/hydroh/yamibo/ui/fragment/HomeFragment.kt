package com.hydroh.yamibo.ui.fragment

import android.app.Fragment
import android.content.*
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.SectorActivity
import com.hydroh.yamibo.ui.adapter.HomeAdapter
import com.hydroh.yamibo.util.parser.HomeParser
import org.jsoup.nodes.Document

const val DEFAULT_URL = "forum.php"

const val ARG_URL = "url"
const val ARG_TITLE = "title"

class HomeFragment : Fragment() {

    private lateinit var mHomeItemList: List<MultiItemEntity>
    private var mPageTitle: String? = null
    private var mPageUrl: String = DEFAULT_URL
    private var mNextPageUrl: String? = null
    private var mListener: InteractListener? = null

    private val mSwipeRefreshLayout by lazy { view!!.findViewById<SwipeRefreshLayout>(R.id.refresh_common) }
    private val mToolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar_home) }
    private val mHintText by lazy { view!!.findViewById<TextView>(R.id.hint_text) }
    private val mLoadProgressBar by lazy { view!!.findViewById<ProgressBar>(R.id.hint_progressbar) }
    private val mContentRecyclerView by lazy { view!!.findViewById<RecyclerView>(R.id.list_common) }

    private val mRefreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mListener?.onHomeRefresh()
            loadHome(mSwipeRefreshLayout)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            mPageUrl = getString(ARG_URL, DEFAULT_URL)
            mPageTitle = getString(ARG_TITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.run {
            if (this is AppCompatActivity) {
                setSupportActionBar(mToolbar)
                title?.let { supportActionBar?.title = it }
            }
        }
        mListener?.onToolbarReady(mToolbar)

        mSwipeRefreshLayout.setOnRefreshListener {
            loadHome(mSwipeRefreshLayout)
        }
        mHintText.setOnClickListener {
            loadHome(it)
        }
        mToolbar.setOnClickListener {
            mContentRecyclerView.scrollToPosition(0)
        }
        loadHome(mHintText)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is InteractListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement InteractListener")
        }
        context.registerReceiver(mRefreshBroadcastReceiver, IntentFilter("com.hydroh.yamibo.REFRESH"))
    }
    override fun onDetach() {
        mListener = null
        activity!!.unregisterReceiver(mRefreshBroadcastReceiver)
        super.onDetach()
    }

    private fun loadHome(view: View) {
        Log.d(ContentValues.TAG, "refreshNetwork: URL: $mPageUrl")

        if (view.id == R.id.hint_text) {
            mHintText.visibility = View.GONE
            mLoadProgressBar.visibility = View.VISIBLE
        } else if (view.id == R.id.refresh_common) {
            mSwipeRefreshLayout.isRefreshing = true
        }

        WebRequest.getHtmlDocument(mPageUrl, false, activity!!, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val homeParser = HomeParser(document)
                mHomeItemList = homeParser.groupList
                mNextPageUrl = homeParser.nextPageUrl
                activity!!.runOnUiThread {
                    mHintText.visibility = View.GONE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false
                    homeParser.run {
                        mListener?.onUserStatReady(isLoggedIn, avatarUrl, username, uid)
                    }

                    val layoutManager = LinearLayoutManager(mContentRecyclerView.context)
                    mContentRecyclerView.layoutManager = layoutManager

                    val adapter = HomeAdapter(mHomeItemList)
                    mContentRecyclerView.adapter = adapter
                    if (activity!! is SectorActivity) {
                        adapter.setOnLoadMoreListener({
                            if (mNextPageUrl != null) {
                                WebRequest.getHtmlDocument(mNextPageUrl!!, false, mContentRecyclerView.context, object : DocumentCallbackListener {
                                    override fun onFinish(document: Document) {
                                        val homeMoreParser = HomeParser(document, true)
                                        activity!!.runOnUiThread {
                                            Log.d(ContentValues.TAG, "post: LoadMore Complete.")
                                            mNextPageUrl = homeMoreParser.nextPageUrl
                                            adapter.addData(homeMoreParser.groupList)
                                            adapter.loadMoreComplete()
                                        }
                                    }

                                    override fun onError(e: Exception) {
                                        activity!!.runOnUiThread {
                                            Log.d(ContentValues.TAG, "post: LoadMore failed.")
                                            adapter.loadMoreFail()
                                        }
                                    }
                                })
                            } else {
                                activity!!.runOnUiThread {
                                    Log.d(ContentValues.TAG, "post: LoadMore End.")
                                    adapter.loadMoreEnd()
                                }
                            }
                        }, mContentRecyclerView)
                    }
                    adapter.expandAll()
                    adapter.collapseSticky()

                    val dividerItemDecoration = DividerItemDecoration(
                            mContentRecyclerView.context,
                            layoutManager.orientation
                    )
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(mContentRecyclerView.context, R.drawable.divider_horizontal_thin)!!)
                    if (mContentRecyclerView.itemDecorationCount == 0) {
                        mContentRecyclerView.addItemDecoration(dividerItemDecoration)
                    }
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                activity!!.runOnUiThread {
                    val adapter = mContentRecyclerView.adapter as HomeAdapter?
                    adapter?.clear()
                    mHintText.visibility = View.VISIBLE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    interface InteractListener {
        fun onHomeRefresh()
        fun onToolbarReady(toolbar: Toolbar)
        fun onUserStatReady(isLoggedIn: Boolean, avatarUrl: String?, username: String?, uid: String?)
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String?, title: String?) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        url?.let {
                            putString(ARG_URL, it)
                        }
                        title?.let {
                            putString(ARG_TITLE, it)
                        }
                    }
                }
    }
}
