package com.hydroh.yamibo.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.PrivateMessageAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshActivity
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.util.parser.PrivateMessageParser
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.activity_private_message.*
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.ctx
import org.jsoup.nodes.Document

class PrivateMessageActivity : AbsRefreshActivity() {

    private lateinit var mMessageList: List<MultiItemEntity>
    private var mUid: String = ""
    private var mUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_message)

        refresh_common.setOnRefreshListener { loadMessages(refresh_common) }
        setSupportActionBar(toolbar_private_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar_private_message.setOnClickListener {
            list_common.scrollToPosition(0)
        }
        list_common.background = ColorDrawable(Color.parseColor("#EEEEEE"))

        intent?.run {
            extras?.run {
                mUid = getString(Constants.ARG_INTENT_UID)
                mUsername = getString(Constants.ARG_INTENT_USERNAME)
            }
        }
        title = if (mUsername != null) "与 $mUsername 的私信" else "私信"
        RichText.initCacheDir(ctx)
        loadMessages(hint_text)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadMessages(view: View) {
        if (view.id == R.id.hint_text) {
            setRefreshState(RefreshState.START_MAIN)
        } else if (view.id == R.id.refresh_common) {
            setRefreshState(RefreshState.START_SWIPE)
        }

        WebRequest.getHtmlDocument(UrlUtils.getMessageMailPrivatePageUrl(mUid), false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val messageParser = PrivateMessageParser(document)
                mMessageList = messageParser.messageList
                runOnUiThread {
                    setRefreshState(RefreshState.FINISH)
                    list_common.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true)
                    list_common.adapter = PrivateMessageAdapter(mMessageList)
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    (list_common.adapter as PrivateMessageAdapter).clear()
                    setRefreshState(RefreshState.ERROR)
                }
            }
        })
    }
}
