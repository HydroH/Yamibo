package com.hydroh.yamibo.ui.fragment

import android.content.*
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.SearchActivity
import com.hydroh.yamibo.ui.SectorActivity
import com.hydroh.yamibo.ui.adapter.HomeAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshListFragment
import com.hydroh.yamibo.ui.common.PageReloadListener
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.parser.HomeParser
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document
import java.lang.RuntimeException

class HomeFragment : AbsRefreshListFragment() {

    private lateinit var mHomeItemList: List<MultiItemEntity>
    private var mPageTitle: String? = null
    private var mPageUrl: String = UrlUtils.getDefaultUrl()
    private var mNextPageUrl: String? = null
    private var mFormHash: String? = null
    private var mListener: HomeInteractListener? = null

    private val mRefreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mListener?.onHomeRefresh()
            refreshList(refresh_common)
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
        mListener?.onSetupToolbar(toolbar_home, mPageTitle)
        toolbar_home.setOnClickListener {
            list_common.scrollToPosition(0)
        }
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
            mListener?.onSetupToolbar(toolbar_home, null) //TODO: But why???
        }
    }

    override fun loadContent() {
        WebRequest.getHtmlDocument(mPageUrl, false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val homeParser = HomeParser(document)
                mHomeItemList = homeParser.groupList
                mNextPageUrl = homeParser.nextPageUrl
                mFormHash = homeParser.formhash
                ctx.runOnUiThread {
                    setRefreshState(RefreshState.FINISH)
                    homeParser.run {
                        mListener?.onUserStatReady(isLoggedIn, avatarUrl, username, uid)
                    }

                    val layoutManager = LinearLayoutManager(ctx)
                    list_common.layoutManager = layoutManager

                    list_common.adapter = HomeAdapter(mHomeItemList).apply {
                        pageReloadListener = object : PageReloadListener {
                            override fun onPageReload(url: String) {
                                mPageUrl = url
                                refreshList(list_common)
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
                            }, list_common)
                        }
                        expandAll()
                        collapseSticky()
                    }

                    val dividerItemDecoration =
                            DividerItemDecoration(ctx, layoutManager.orientation).apply {
                                setDrawable(ContextCompat.getDrawable(ctx, R.drawable.divider_horizontal_thin)!!)
                            }
                    if (list_common.itemDecorationCount == 0) {
                        list_common.addItemDecoration(dividerItemDecoration)
                    }
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                ctx.runOnUiThread {
                    val adapter = list_common.adapter as HomeAdapter?
                    adapter?.clear()
                    setRefreshState(RefreshState.ERROR)
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
