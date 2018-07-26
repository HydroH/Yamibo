package com.hydroh.yamibo.ui.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.FavoriteAdapter
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.parser.FavoriteParser
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document
import java.lang.RuntimeException

class FavoriteFragment : Fragment() {

    private lateinit var mFavoritePostList: List<MultiItemEntity>
    private var mNextPageUrl: String? = null
    private var mListener: HomeInteractListener? = null

    private val mSwipeRefreshLayout by lazy { view!!.find<SwipeRefreshLayout>(R.id.refresh_common) }
    private val mToolbar by lazy { view!!.find<Toolbar>(R.id.toolbar_favorite) }
    private val mHintText by lazy { view!!.find<TextView>(R.id.hint_text) }
    private val mLoadProgressBar by lazy { view!!.find<ProgressBar>(R.id.hint_progressbar) }
    private val mContentRecyclerView by lazy { view!!.find<RecyclerView>(R.id.list_common) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_favorite, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListener?.onSetupToolbar(mToolbar, "收藏夹")
        mSwipeRefreshLayout.setOnRefreshListener {
            loadFavorite(mSwipeRefreshLayout)
        }
        mHintText.setOnClickListener {
            loadFavorite(it)
        }
        mToolbar.setOnClickListener {
            mContentRecyclerView.scrollToPosition(0)
        }
        loadFavorite(mHintText)
    }

    private fun loadFavorite(view: View) {
        if (view.id == R.id.hint_text) {
            mHintText.visibility = View.GONE
            mLoadProgressBar.visibility = View.VISIBLE
        } else if (view.id == R.id.refresh_common) {
            mSwipeRefreshLayout.isRefreshing = true
        }

        WebRequest.getHtmlDocument(UrlUtils.getFavoritePageUrl(), false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val favoriteParser = FavoriteParser(document)
                mFavoritePostList = favoriteParser.favoriteList
                mNextPageUrl = favoriteParser.nextPageUrl

                ctx.runOnUiThread {
                    mHintText.visibility = View.GONE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false

                    val layoutManager = LinearLayoutManager(ctx)
                    mContentRecyclerView.layoutManager = layoutManager

                    mContentRecyclerView.adapter = FavoriteAdapter(mFavoritePostList).apply {
                        setOnLoadMoreListener({
                            if (mNextPageUrl != null) {
                                WebRequest.getHtmlDocument(mNextPageUrl!!, false, ctx, object : DocumentCallbackListener {
                                    override fun onFinish(document: Document) {
                                        val favoriteMoreParser = FavoriteParser(document)
                                        ctx.runOnUiThread {
                                            mNextPageUrl = favoriteMoreParser.nextPageUrl
                                            addData(favoriteMoreParser.favoriteList)
                                            loadMoreComplete()
                                        }
                                    }

                                    override fun onError(e: Exception) {
                                        ctx.runOnUiThread {
                                            loadMoreFail()
                                        }
                                    }
                                })
                            } else {
                                ctx.runOnUiThread {
                                    loadMoreEnd()
                                }
                            }
                        }, mContentRecyclerView)
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
                    val adapter = mContentRecyclerView.adapter as FavoriteAdapter?
                    adapter?.clear()
                    mHintText.visibility = View.VISIBLE
                    mLoadProgressBar.visibility = View.GONE
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeInteractListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement InteractListener")
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }
}
