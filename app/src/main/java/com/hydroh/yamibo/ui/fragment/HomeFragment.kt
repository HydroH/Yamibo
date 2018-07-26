package com.hydroh.yamibo.ui.fragment

import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.SearchActivity
import com.hydroh.yamibo.ui.SectorActivity
import com.hydroh.yamibo.ui.adapter.HomeAdapter
import com.hydroh.yamibo.ui.common.PageReloadListener
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.parser.HomeParser
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document
import java.lang.RuntimeException

class HomeFragment : Fragment() {

    private lateinit var mHomeItemList: List<MultiItemEntity>
    private var mPageTitle: String? = null
    private var mPageUrl: String = UrlUtils.getDefaultUrl()
    private var mNextPageUrl: String? = null
    private var mFormHash: String? = null
    private var mListener: HomeInteractListener? = null

    private val mSwipeRefreshLayout by lazy { view!!.find<SwipeRefreshLayout>(R.id.refresh_common) }
    private val mToolbar by lazy { view!!.find<Toolbar>(R.id.toolbar_home) }
    private val mHintText by lazy { view!!.find<TextView>(R.id.hint_text) }
    private val mLoadProgressBar by lazy { view!!.find<ProgressBar>(R.id.hint_progressbar) }
    private val mContentRecyclerView by lazy { view!!.find<RecyclerView>(R.id.list_common) }

    private val mRefreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mListener?.onHomeRefresh()
            loadHome(mSwipeRefreshLayout)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            mPageUrl = getString(Constants.ARG_INTENT_URL, UrlUtils.getDefaultUrl())
            mPageTitle = getString(Constants.ARG_INTENT_TITLE)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListener?.onSetupToolbar(mToolbar, mPageTitle)
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
        if (context is HomeInteractListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement InteractListener")
        }
        context.registerReceiver(mRefreshBroadcastReceiver, IntentFilter("com.hydroh.yamibo.REFRESH"))
    }

    override fun onDetach() {
        mListener = null
        ctx.unregisterReceiver(mRefreshBroadcastReceiver)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_toolbar_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> {
                val intent = Intent(activity!!, SearchActivity::class.java)
                intent.putExtra(Constants.ARG_INTENT_FORMHASH, mFormHash)
                if (!UrlUtils.isDefaultUrl(mPageUrl)) {
                    Regex("-(\\d)+-").find(mPageUrl)?.groupValues?.get(1)?.let {
                        intent.putExtra(Constants.ARG_INTENT_FID, it)
                                .putExtra(Constants.ARG_INTENT_NAME, mPageTitle)
                    }
                }
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mListener?.onSetupToolbar(mToolbar, null) //TODO: But why???
        }
    }

    private fun loadHome(view: View) {
        if (view.id == R.id.hint_text) {
            mHintText.visibility = View.GONE
            mLoadProgressBar.visibility = View.VISIBLE
        } else if (view.id == R.id.refresh_common) {
            mSwipeRefreshLayout.isRefreshing = true
        }

        WebRequest.getHtmlDocument(mPageUrl, false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val homeParser = HomeParser(document)
                mHomeItemList = homeParser.groupList
                mNextPageUrl = homeParser.nextPageUrl
                mFormHash = homeParser.formhash
                ctx.runOnUiThread {
                    mHintText.visibility = View.GONE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false
                    homeParser.run {
                        mListener?.onUserStatReady(isLoggedIn, avatarUrl, username, uid)
                    }

                    val layoutManager = LinearLayoutManager(ctx)
                    mContentRecyclerView.layoutManager = layoutManager

                    mContentRecyclerView.adapter = HomeAdapter(mHomeItemList).apply {
                        pageReloadListener = object : PageReloadListener {
                            override fun onPageReload(url: String) {
                                mPageUrl = url
                                loadHome(refresh_common)
                            }
                        }

                        if (activity!! is SectorActivity) {
                            setOnLoadMoreListener({
                                if (mNextPageUrl != null) {
                                    WebRequest.getHtmlDocument(mNextPageUrl!!, false, ctx, object : DocumentCallbackListener {
                                        override fun onFinish(document: Document) {
                                            val homeMoreParser = HomeParser(document, true)
                                            ctx.runOnUiThread {
                                                Log.d(ContentValues.TAG, "post: LoadMore Complete.")
                                                mNextPageUrl = homeMoreParser.nextPageUrl
                                                mFormHash = homeMoreParser.formhash
                                                addData(homeMoreParser.groupList)
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
                        expandAll()
                        collapseSticky()
                    }

                    val dividerItemDecoration =
                            DividerItemDecoration(ctx, layoutManager.orientation).apply {
                                setDrawable(ContextCompat.getDrawable(ctx, R.drawable.divider_horizontal_thin)!!)
                            }
                    if (mContentRecyclerView.itemDecorationCount == 0) {
                        mContentRecyclerView.addItemDecoration(dividerItemDecoration)
                    }
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                ctx.runOnUiThread {
                    val adapter = mContentRecyclerView.adapter as HomeAdapter?
                    adapter?.clear()
                    mHintText.visibility = View.VISIBLE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String?, title: String?) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        url?.let {
                            putString(Constants.ARG_INTENT_URL, it)
                        }
                        title?.let {
                            putString(Constants.ARG_INTENT_TITLE, it)
                        }
                    }
                }
    }
}
