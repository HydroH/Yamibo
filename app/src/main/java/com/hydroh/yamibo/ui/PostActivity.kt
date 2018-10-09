package com.hydroh.yamibo.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.Reply
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.PostAdapter
import com.hydroh.yamibo.ui.common.AbsRefreshActivity
import com.hydroh.yamibo.ui.common.RefreshState
import com.hydroh.yamibo.util.PrefUtils
import com.hydroh.yamibo.util.parser.PostParser
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.*
import org.jsoup.nodes.Document

class PostActivity : AbsRefreshActivity() {

    private lateinit var mReplyList: List<MultiItemEntity>
    private lateinit var mImgUrlList: ArrayList<String>
    private var mPageUrl: String = ""
    private var mPrevPageUrl: String? = null
    private var mNextPageUrl: String? = null
    private var mPageJumpUrl: String? = null
    private var mPageCount: Int = 1
    private var mCurrentPage: Int = 1
    private var mAuthorOnlyUrl: String? = null
    private var mPageOrigUrl: String? = null
    private var mReplyUrl: String? = null
    private var mFormHash: String? = null

    private var mTitleTextView : TextView? = null

    private val animatorDim by lazy {
        ObjectAnimator.ofInt(list_post.foreground, "alpha", 0, 255)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        refresh_common.setOnRefreshListener { loadPosts(refresh_common) }
        setSupportActionBar(toolbar_post)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar_post.setOnClickListener {
            list_common.scrollToPosition(0)
        }
        edit_post_reply.setOnFocusChangeListener { view, b -> setFocusEditReply(b) }
        list_post.foreground = ColorDrawable(ContextCompat.getColor(ctx, R.color.blackTransHalf))
        list_post.foreground.alpha = 0
        animatorDim.duration = 200

        intent?.run {
            if (action == Intent.ACTION_VIEW) {
                val uri = data
                mPageUrl = uri.path
            } else {
                extras?.run {
                    mPageUrl = getString(Constants.ARG_INTENT_URL, "")
                    title = getString(Constants.ARG_INTENT_TITLE, "百合会")
                }
            }
        }
        mPageOrigUrl = mPageUrl
        Log.d(TAG, "onCreate: URL: $mPageUrl")

        try {
            val field = toolbar_post::class.java.getDeclaredField("mTitleTextView")
            field.isAccessible = true
            mTitleTextView = (field.get(toolbar_post) as TextView).apply {
                ellipsize = TextUtils.TruncateAt.MARQUEE
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                setSingleLine(true)
                isSelected = true
                marqueeRepeatLimit = -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        RichText.initCacheDir(ctx)
        loadPosts(hint_text)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_link -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText("网页地址", UrlUtils.getFullUrl(mPageUrl))
                toast("链接已复制")
            }
            R.id.menu_page -> {
                mPageJumpUrl?.let {
                    alert("请输入要跳转到的页面：") {
                        lateinit var inputWidget: NumberPicker
                        title = "跳转至页面"
                        isCancelable = true
                        customView {
                            relativeLayout {
                                inputWidget = numberPicker {
                                    minValue = 1
                                    maxValue = mPageCount
                                    value = mCurrentPage
                                    layoutParams = RelativeLayout.LayoutParams(
                                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                                            RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                                        addRule(RelativeLayout.CENTER_HORIZONTAL)
                                    }
                                }
                            }
                        }
                        positiveButton("确定") {
                            it.dismiss()
                            inputWidget.clearFocus()
                            mPageUrl = mPageJumpUrl + inputWidget.value
                            loadPosts(hint_text, updateHistory = false)
                        }
                        negativeButton("取消") {
                            it.dismiss()
                        }
                    }.show()
                }
            }
            R.id.menu_author_only -> {
                mPageUrl = (if (mPageUrl == mAuthorOnlyUrl) mPageOrigUrl else mAuthorOnlyUrl) ?: ""
                loadPosts(hint_text, updateHistory = false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPosts(view: View, updateHistory: Boolean = true) {
        Log.d(TAG, "refreshNetwork: URL: $mPageUrl")

        if (view.id == R.id.hint_text) {
            setRefreshState(RefreshState.START_MAIN)
        } else if (view.id == R.id.refresh_common) {
            setRefreshState(RefreshState.START_SWIPE)
        }

        WebRequest.getHtmlDocument(mPageUrl, false, ctx, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val postParser = PostParser(document)
                val postInfo = postParser.run {
                    (replyList.first() as Reply).run {
                        Post(title ?: this@PostActivity.title.toString(), "", author, "", 0, mPageUrl, sector ?: "", "")
                    }
                }
                if (updateHistory) PrefUtils.updatePostHistory(ctx, postInfo)

                mReplyList = postParser.replyList
                mImgUrlList = postParser.imgUrlList
                mPrevPageUrl = postParser.prevPageUrl
                mNextPageUrl = postParser.nextPageUrl
                mPageJumpUrl = postParser.pageJumpUrl
                mPageCount = postParser.pageCount
                mCurrentPage = postParser.currentPage
                mAuthorOnlyUrl = postParser.authorOnlyUrl

                mReplyUrl = postParser.replyUrl
                mFormHash = postParser.formhash
                if (mReplyUrl != null && mFormHash != null) {
                    button_post_reply.setOnClickListener {
                        if (edit_post_reply.text.isEmpty()) return@setOnClickListener
                        WebRequest.postReply(mReplyUrl!!, edit_post_reply.text.toString(), mFormHash!!,
                                ctx, object : DocumentCallbackListener {
                            override fun onFinish(document: Document) {
                                runOnUiThread {
                                    toast("回复成功")
                                    edit_post_reply.clearComposingText()
                                    setFocusEditReply(false)
                                }
                            }
                            override fun onError(e: Exception) {
                                runOnUiThread {
                                    toast("回复失败")
                                }
                            }
                        })
                    }
                }

                runOnUiThread {
                    setRefreshState(RefreshState.FINISH)
                    list_common.layoutManager = LinearLayoutManager(ctx)

                    postParser.title?.let {
                        if (title != it) {
                            title = it
                        }
                    }

                    list_common.adapter = PostAdapter(mReplyList, mImgUrlList).apply {
                        setOnLoadMoreListener({
                            if (mNextPageUrl != null) {
                                WebRequest.getHtmlDocument(mNextPageUrl!!, false, ctx, object : DocumentCallbackListener {
                                    override fun onFinish(document: Document) {
                                        val postMoreParser = PostParser(document)
                                        list_common.post {
                                            Log.d(TAG, "post: LoadMore Complete.")
                                            mNextPageUrl = postMoreParser.nextPageUrl
                                            mPageCount = postMoreParser.pageCount
                                            mCurrentPage = postMoreParser.currentPage
                                            mFormHash = postMoreParser.formhash
                                            addData(postMoreParser.replyList)
                                            mImgUrlList.addAll(postMoreParser.imgUrlList)
                                            loadMoreComplete()
                                        }
                                    }
                                    override fun onError(e: Exception) {
                                        list_common.post {
                                            Log.d(TAG, "post: LoadMore failed.")
                                            loadMoreFail()
                                        }
                                    }
                                })
                            } else {
                                list_common.post {
                                    Log.d(TAG, "post: LoadMore End.")
                                    loadMoreEnd()
                                }
                            }
                        }, list_common)

                        if (mPrevPageUrl != null) {
                            refresh_common.isEnabled = false
                            isUpFetchEnable = true
                            setUpFetchListener {
                                isUpFetching = true

                                WebRequest.getHtmlDocument(mPrevPageUrl!!, false, ctx, object : DocumentCallbackListener {
                                    override fun onFinish(document: Document) {
                                        val postMoreParser = PostParser(document)
                                        list_common.post {
                                            Log.d(TAG, "post: LoadMore Complete.")
                                            mPrevPageUrl = postMoreParser.prevPageUrl
                                            mPageCount = postMoreParser.pageCount
                                            mCurrentPage = postMoreParser.currentPage
                                            mFormHash = postMoreParser.formhash
                                            addData(0, postMoreParser.replyList)
                                            mImgUrlList.addAll(0, postMoreParser.imgUrlList)
                                            isUpFetching = false
                                            if (mPrevPageUrl == null) {
                                                isUpFetchEnable = false
                                                refresh_common.isEnabled = true
                                            }
                                        }
                                    }

                                    override fun onError(e: Exception) {
                                        list_common.post {
                                            Log.d(TAG, "post: LoadMore failed.")
                                            isUpFetching = false
                                        }
                                    }
                                })
                            }
                        } else {
                            isUpFetchEnable = false
                            refresh_common.isEnabled = true
                        }
                    }
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    (list_common.adapter as PostAdapter?)?.clear()
                    setRefreshState(RefreshState.ERROR)
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFocusEditReply(focused: Boolean) {
        if (focused) {
            edit_post_reply.setLines(3)
            button_post_reply.visibility = View.VISIBLE
            list_post.isInterceptTouchEvent = true
            list_post.setOnTouchListener { view, motionEvent ->
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edit_post_reply.windowToken, 0)
                edit_post_reply.clearFocus()
                Log.d(TAG, "onTouch: Post Layout touched. Shrinking edittext...")
                return@setOnTouchListener true
            }
            animatorDim.setIntValues(0, 255)
            animatorDim.start()
        } else {
            edit_post_reply.setLines(1)
            button_post_reply.visibility = View.GONE
            list_post.setOnTouchListener(null)
            list_post.isInterceptTouchEvent = false
            animatorDim.setIntValues(255, 0)
            animatorDim.start()
        }
    }

}
