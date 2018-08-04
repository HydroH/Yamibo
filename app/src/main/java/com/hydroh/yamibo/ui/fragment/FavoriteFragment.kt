package com.hydroh.yamibo.ui.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.FavoriteAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshListFragment
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.parser.FavoriteParser
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document
import java.lang.RuntimeException

class FavoriteFragment : AbsRefreshListFragment() {

    private lateinit var mFavoritePostList: List<MultiItemEntity>
    private var mNextPageUrl: String? = null
    private var mListener: HomeInteractListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_favorite, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListener?.onSetupToolbar(toolbar_favorite, "收藏夹")
        toolbar_favorite.setOnClickListener {
            list_common.scrollToPosition(0)
        }
    }

    override fun loadContent() {
        WebRequest.getHtmlDocument(UrlUtils.getFavoritePageUrl(), false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val favoriteParser = FavoriteParser(document)
                mFavoritePostList = favoriteParser.favoriteList
                mNextPageUrl = favoriteParser.nextPageUrl

                ctx.runOnUiThread {
                    setRefreshState(RefreshState.FINISH)
                    val layoutManager = LinearLayoutManager(ctx)
                    list_common.layoutManager = layoutManager

                    list_common.adapter = FavoriteAdapter(mFavoritePostList).apply {
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
                        }, list_common)
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
                    (list_common.adapter as FavoriteAdapter?)?.clear()
                    setRefreshState(RefreshState.ERROR)
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
