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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.model.Reply
import com.hydroh.yamibo.network.UrlUtils
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.PostAdapter
import com.hydroh.yamibo.util.PrefUtils
import com.hydroh.yamibo.util.parser.PostParser
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.list_common.*
import org.jsoup.nodes.Document

class PostActivity : AppCompatActivity() {

    private lateinit var mReplyList: List<MultiItemEntity>
    private lateinit var mImgUrlList: ArrayList<String>
    private var mPageUrl: String = ""
    private var mPrevPageUrl: String? = null
    private var mNextPageUrl: String? = null
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
        list_post.foreground = ColorDrawable(ContextCompat.getColor(this, R.color.blackTransHalf))
        list_post.foreground.alpha = 0
        animatorDim.duration = 200

        intent?.let {
            if (intent.action == Intent.ACTION_VIEW) {
                val uri = intent.data
                mPageUrl = uri.path
            } else {
                intent.extras?.run {
                    mPageUrl = getString(Constants.ARG_INTENT_URL, "")
                    title = getString(Constants.ARG_INTENT_TITLE, "百合会")
                }
            }
        }
        Log.d(TAG, "onCreate: URL: $mPageUrl")

        try {
            val field = toolbar_post::class.java.getDeclaredField("mTitleTextView")
            field.isAccessible = true
            mTitleTextView = field.get(toolbar_post) as TextView

            mTitleTextView!!.ellipsize = TextUtils.TruncateAt.MARQUEE
            mTitleTextView!!.isFocusable = true
            mTitleTextView!!.isFocusableInTouchMode = true
            mTitleTextView!!.requestFocus()
            mTitleTextView!!.setSingleLine(true)
            mTitleTextView!!.isSelected = true
            mTitleTextView!!.marqueeRepeatLimit = -1
        } catch (e: Exception) {
            e.printStackTrace()
        }

        RichText.initCacheDir(this)
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
                Toast.makeText(this, "链接已复制", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPosts(view: View) {
        Log.d(TAG, "refreshNetwork: URL: $mPageUrl")

        if (view.id == R.id.hint_text) {
            hint_text.visibility = View.GONE
            hint_progressbar.visibility = View.VISIBLE
        }

        WebRequest.getHtmlDocument(mPageUrl, false, this, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val postParser = PostParser(document)
                val postInfo = postParser.run {
                    (replyList.first() as Reply).run {
                        Post(title ?: this@PostActivity.title.toString(), "", author, postDate, 0, mPageUrl, "", "")
                    }
                }
                PrefUtils.updatePostHistory(this@PostActivity, postInfo)

                mReplyList = postParser.replyList
                mImgUrlList = postParser.imgUrlList
                mPrevPageUrl = postParser.prevPageUrl
                mNextPageUrl = postParser.nextPageUrl

                mReplyUrl = postParser.replyUrl
                mFormHash = postParser.formhash
                if (mReplyUrl != null && mFormHash != null) {
                    button_post_reply.setOnClickListener {
                        if (edit_post_reply.text.isEmpty()) return@setOnClickListener
                        WebRequest.postReply(mReplyUrl!!, edit_post_reply.text.toString(), mFormHash!!,
                                this@PostActivity, object : DocumentCallbackListener {
                            override fun onFinish(document: Document) {
                                runOnUiThread {
                                    Toast.makeText(this@PostActivity, "回复成功", Toast.LENGTH_SHORT).show()
                                    edit_post_reply.clearComposingText()
                                    setFocusEditReply(false)
                                }
                            }
                            override fun onError(e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(this@PostActivity, "回复失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }

                runOnUiThread {
                    hint_progressbar.visibility = View.GONE
                    refresh_common.isRefreshing = false
                    val layoutManager = LinearLayoutManager(list_common.context)
                    list_common.layoutManager = layoutManager

                    postParser.title?.let {
                        if (title != it) {
                            title = it
                        }
                    }

                    val adapter = PostAdapter(mReplyList, mImgUrlList)
                    list_common.adapter = adapter
                    adapter.setOnLoadMoreListener({
                        if (mNextPageUrl != null) {
                            WebRequest.getHtmlDocument(mNextPageUrl!!, false, list_common.context, object : DocumentCallbackListener {
                                override fun onFinish(document: Document) {
                                    val postMoreParser = PostParser(document)
                                    list_common.post {
                                        Log.d(TAG, "post: LoadMore Complete.")
                                        mNextPageUrl = postMoreParser.nextPageUrl
                                        mFormHash = postMoreParser.formhash
                                        adapter.addData(postMoreParser.replyList)
                                        mImgUrlList.addAll(postMoreParser.imgUrlList)
                                        adapter.loadMoreComplete()
                                    }
                                }
                                override fun onError(e: Exception) {
                                    list_common.post {
                                        Log.d(TAG, "post: LoadMore failed.")
                                        adapter.loadMoreFail()
                                    }
                                }
                            })
                        } else {
                            list_common.post {
                                Log.d(TAG, "post: LoadMore End.")
                                adapter.loadMoreEnd()
                            }
                        }
                    }, list_common)

                    if (mPrevPageUrl != null) {
                        refresh_common.isEnabled = false
                        adapter.isUpFetchEnable = true
                        adapter.setUpFetchListener {
                            adapter.isUpFetching = true

                            WebRequest.getHtmlDocument(mPrevPageUrl!!, false, list_common.context, object : DocumentCallbackListener {
                                override fun onFinish(document: Document) {
                                    val postMoreParser = PostParser(document)
                                    list_common.post {
                                        Log.d(TAG, "post: LoadMore Complete.")
                                        mPrevPageUrl = postMoreParser.prevPageUrl
                                        mFormHash = postMoreParser.formhash
                                        adapter.addData(0, postMoreParser.replyList)
                                        mImgUrlList.addAll(0, postMoreParser.imgUrlList)
                                        adapter.isUpFetching = false
                                        if (mPrevPageUrl == null) {
                                            adapter.isUpFetchEnable = false
                                            refresh_common.isEnabled = true
                                        }
                                    }
                                }

                                override fun onError(e: Exception) {
                                    list_common.post {
                                        Log.d(TAG, "post: LoadMore failed.")
                                        adapter.isUpFetching = false
                                    }
                                }
                            })
                        }
                    } else {
                        adapter.isUpFetchEnable = false
                        refresh_common.isEnabled = true
                    }
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    val adapter = list_common.adapter as PostAdapter
                    adapter.clear()
                    hint_text.visibility = View.VISIBLE
                    hint_progressbar.visibility = View.GONE
                    refresh_common.isRefreshing = false
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFocusEditReply(focused: Boolean) {
        if (focused) {
            edit_post_reply.setLines(3)
            button_post_reply.visibility = View.VISIBLE
            list_post.isInterCeptTouchEvent = true
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
            list_post.isInterCeptTouchEvent = false
            animatorDim.setIntValues(255, 0)
            animatorDim.start()
        }
    }

}
