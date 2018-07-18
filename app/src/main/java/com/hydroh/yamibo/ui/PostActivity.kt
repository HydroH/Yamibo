package com.hydroh.yamibo.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.PostAdapter
import com.hydroh.yamibo.util.parser.PostParser
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.list_common.*
import org.jsoup.nodes.Document

class PostActivity : AppCompatActivity() {

    private lateinit var replyList: List<MultiItemEntity>
    private lateinit var imgUrlList: ArrayList<String>
    private var url: String = ""
    private var prevPageUrl: String? = null
    private var nextPageUrl: String? = null
    private var replyUrl: String? = null
    private var formhash: String? = null

    private var titleTextView : TextView? = null

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
                url = uri.path
            } else {
                intent.extras?.let {
                    url = it.getString("url", "")
                    title = it.getString("title", "百合会")
                }
            }
        }
        Log.d(TAG, "onCreate: URL: $url")

        try {
            val field = toolbar_post::class.java.getDeclaredField("mTitleTextView")
            field.isAccessible = true
            titleTextView = field.get(toolbar_post) as TextView

            titleTextView!!.ellipsize = TextUtils.TruncateAt.MARQUEE
            titleTextView!!.isFocusable = true
            titleTextView!!.isFocusableInTouchMode = true
            titleTextView!!.requestFocus()
            titleTextView!!.setSingleLine(true)
            titleTextView!!.isSelected = true
            titleTextView!!.marqueeRepeatLimit = -1
        } catch (e: Exception) {
            e.printStackTrace()
        }

        RichText.initCacheDir(this)
        loadPosts(hint_text)
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

    private fun loadPosts(view: View) {
        Log.d(TAG, "refreshNetwork: URL: $url")

        if (view.id == R.id.hint_text) {
            hint_text.visibility = View.GONE
            hint_progressbar.visibility = View.VISIBLE
        }

        WebRequest.getHtmlDocument(url, false, this, object : DocumentCallbackListener {
            override fun onFinish(document: Document) {
                val postParser = PostParser(document)
                replyList = postParser.replyList
                imgUrlList = postParser.imgUrlList
                prevPageUrl = postParser.prevPageUrl
                nextPageUrl = postParser.nextPageUrl

                replyUrl = postParser.replyUrl
                formhash = postParser.formhash
                if (replyUrl != null && formhash != null) {
                    button_post_reply.setOnClickListener {
                        if (edit_post_reply.text.isEmpty()) return@setOnClickListener
                        WebRequest.postReply(replyUrl!!, edit_post_reply.text.toString(), formhash!!,
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

                    val adapter = PostAdapter(replyList, imgUrlList)
                    list_common.adapter = adapter
                    adapter.setOnLoadMoreListener({
                        if (nextPageUrl != null) {
                            WebRequest.getHtmlDocument(nextPageUrl!!, false, list_common.context, object : DocumentCallbackListener {
                                override fun onFinish(document: Document) {
                                    val postMoreParser = PostParser(document)
                                    list_common.post {
                                        Log.d(TAG, "post: LoadMore Complete.")
                                        nextPageUrl = postMoreParser.nextPageUrl
                                        adapter.addData(postMoreParser.replyList)
                                        imgUrlList.addAll(postMoreParser.imgUrlList)
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

                    if (prevPageUrl != null) {
                        refresh_common.isEnabled = false
                        adapter.isUpFetchEnable = true
                        adapter.setUpFetchListener {
                            adapter.isUpFetching = true

                            WebRequest.getHtmlDocument(prevPageUrl!!, false, list_common.context, object : DocumentCallbackListener {
                                override fun onFinish(document: Document) {
                                    val postMoreParser = PostParser(document)
                                    list_common.post {
                                        Log.d(TAG, "post: LoadMore Complete.")
                                        prevPageUrl = postMoreParser.prevPageUrl
                                        adapter.addData(0, postMoreParser.replyList)
                                        imgUrlList.addAll(0, postMoreParser.imgUrlList)
                                        adapter.isUpFetching = false
                                        if (prevPageUrl == null) {
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
