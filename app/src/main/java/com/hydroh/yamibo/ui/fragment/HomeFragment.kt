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
import com.hydroh.yamibo.util.parser.HomeParser
import kotlinx.android.synthetic.main.list_common.*
import org.jsoup.nodes.Document
import java.lang.RuntimeException

class HomeFragment : Fragment() {

    private lateinit var mHomeItemList: List<MultiItemEntity>
    private var mPageTitle: String? = null
    private var mPageUrl: String = UrlUtils.getDefaultUrl()
    private var mNextPageUrl: String? = null
    private var mFormHash: String? = null
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
            mPageUrl = getString(Constants.ARG_INTENT_URL, UrlUtils.getDefaultUrl())
            mPageTitle = getString(Constants.ARG_INTENT_TITLE)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.run {
            if (this is AppCompatActivity) {
                setSupportActionBar(mToolbar)
                mPageTitle?.let { supportActionBar?.title = it }
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
                mFormHash = homeParser.formhash
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
                    adapter.pageReloadListener = object : PageReloadListener {
                        override fun onPageReload(url: String) {
                            mPageUrl = url
                            loadHome(refresh_common)
                        }
                    }

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
                                            mFormHash = homeMoreParser.formhash
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
                            putString(Constants.ARG_INTENT_URL, it)
                        }
                        title?.let {
                            putString(Constants.ARG_INTENT_TITLE, it)
                        }
                    }
                }
    }
}
