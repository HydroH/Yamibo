package com.hydroh.yamibo.ui.fragment


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.MessageMailAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshListFragment
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.util.parser.MessageMailParser
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import org.jsoup.nodes.Document

class MessageMailFragment : AbsRefreshListFragment() {

    private lateinit var mMailList: List<MultiItemEntity>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_message_mail, container, false)

    override fun loadContent() {
        WebRequest.getHtmlDocument(UrlUtils.getMessageMailPageUrl(), false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val messageMailParser = MessageMailParser(document)
                mMailList = messageMailParser.mailList
                ctx.runOnUiThread {
                    setRefreshState(RefreshState.FINISH)

                    list_common.layoutManager = LinearLayoutManager(ctx)
                    list_common.adapter = MessageMailAdapter(mMailList)
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                ctx.runOnUiThread {
                    (list_common.adapter as MessageMailAdapter?)?.clear()
                    setRefreshState(RefreshState.ERROR)
                }
            }
        })
    }

}
