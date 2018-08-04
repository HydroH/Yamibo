package com.hydroh.yamibo.ui.fragment


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
import com.hydroh.yamibo.ui.adapter.MessageReplyAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshListFragment
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.util.parser.MessageReplyParser
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document

class MessageReplyFragment : AbsRefreshListFragment() {

    private lateinit var mReplyList: List<MultiItemEntity>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_message_reply, container, false)

    override fun loadContent() {
        WebRequest.getHtmlDocument(UrlUtils.getMessageReplyPageUrl(), false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val messageReplyParser = MessageReplyParser(document)
                mReplyList = messageReplyParser.replyList
                ctx.runOnUiThread {
                    setRefreshState(RefreshState.FINISH)

                    val layoutManager = LinearLayoutManager(ctx)
                    list_common.layoutManager = layoutManager
                    list_common.adapter = MessageReplyAdapter(mReplyList)

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
                    (list_common.adapter as MessageReplyAdapter?)?.clear()
                    setRefreshState(RefreshState.ERROR)
                }
            }
        })
    }

}
