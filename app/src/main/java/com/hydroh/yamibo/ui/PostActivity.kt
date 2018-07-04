package com.hydroh.yamibo.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.ui.adapter.PostAdapter
import com.hydroh.yamibo.ui.view.ModalFrameLayout
import com.hydroh.yamibo.util.DocumentParser
import com.zzhoujay.richtext.RichText

class PostActivity : AppCompatActivity() {

    private lateinit var replyList: List<MultiItemEntity>
    private lateinit var imgUrlList: ArrayList<String>
    private var url: String = ""
    private var nextPageUrl: String? = null

    private val swipeRefreshLayout by lazy { findViewById<SwipeRefreshLayout>(R.id.refresh_common) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_post) }
    private val hintTextView by lazy { findViewById<TextView>(R.id.hint_text) }
    private val hintProgressBar by lazy { findViewById<ProgressBar>(R.id.hint_progressbar) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.list_common) }
    private val replyEditText by lazy { findViewById<EditText>(R.id.edit_post_reply) }
    private val replySendBtn by lazy { findViewById<ImageButton>(R.id.button_post_reply) }
    private val postLayout by lazy { findViewById<ModalFrameLayout>(R.id.list_post) }
    private var titleTextView : TextView? = null

    private val animatorDim by lazy {
        ObjectAnimator.ofInt(postLayout.foreground, "alpha", 0, 255)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        swipeRefreshLayout.setOnRefreshListener { loadPosts(swipeRefreshLayout) }
        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.post_toolbar_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setOnClickListener {
            recyclerView.scrollToPosition(0)
        }
        replyEditText.setOnFocusChangeListener { view, b -> setFocusEditReply(b) }
        postLayout.foreground = ColorDrawable(ContextCompat.getColor(this, R.color.blackTransHalf))
        postLayout.foreground.alpha = 0
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
            val field = toolbar::class.java.getDeclaredField("mTitleTextView")
            field.isAccessible = true
            titleTextView = field.get(toolbar) as TextView

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
        loadPosts(hintTextView)
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
            hintTextView.visibility = View.GONE
            hintProgressBar.visibility = View.VISIBLE
        }

        WebRequest.getHtmlDocument(url, false, this, object : DocumentCallbackListener {
            override fun onFinish(docParser: DocumentParser) {
                replyList = docParser.parsePost()
                imgUrlList = docParser.imgUrlList
                nextPageUrl = docParser.nextPageUrl
                runOnUiThread {
                    hintProgressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    val layoutManager = LinearLayoutManager(recyclerView.context)
                    recyclerView.layoutManager = layoutManager

                    docParser.title?.let {
                        if (title != it) {
                            title = it
                        }
                    }

                    val adapter = PostAdapter(replyList, imgUrlList)
                    recyclerView.adapter = adapter
                    adapter.setOnLoadMoreListener({
                        if (nextPageUrl != null) {
                            WebRequest.getHtmlDocument(nextPageUrl!!, false, recyclerView.context, object : DocumentCallbackListener {
                                override fun onFinish(docParser: DocumentParser) {
                                    val postMoreList = docParser.parsePost()
                                    recyclerView.post {
                                        Log.d(TAG, "post: LoadMore Complete.")
                                        nextPageUrl = docParser.nextPageUrl
                                        adapter.addData(postMoreList)
                                        imgUrlList.addAll(docParser.imgUrlList)
                                        adapter.loadMoreComplete()
                                    }
                                }
                                override fun onError(e: Exception) {
                                    recyclerView.post {
                                        Log.d(TAG, "post: LoadMore failed.")
                                        adapter.loadMoreFail()
                                    }
                                }
                            })
                        } else {
                            recyclerView.post {
                                Log.d(TAG, "post: LoadMore End.")
                                adapter.loadMoreEnd()
                            }
                        }
                    }, recyclerView)
                }
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    val adapter = recyclerView.adapter as PostAdapter
                    adapter.clear()
                    hintTextView.visibility = View.VISIBLE
                    hintProgressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFocusEditReply(focused: Boolean) {
        if (focused) {
            replyEditText.setLines(3)
            replySendBtn.visibility = View.VISIBLE
            postLayout.isInterCeptTouchEvent = true
            postLayout.setOnTouchListener { view, motionEvent ->
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(replyEditText.windowToken, 0)
                replyEditText.clearFocus()
                Log.d(TAG, "onTouch: Post Layout touched. Shrinking edittext...")
                return@setOnTouchListener true
            }
            animatorDim.setIntValues(0, 255)
            animatorDim.start()
        } else {
            replyEditText.setLines(1)
            replySendBtn.visibility = View.GONE
            postLayout.setOnTouchListener(null)
            postLayout.isInterCeptTouchEvent = false
            animatorDim.setIntValues(255, 0)
            animatorDim.start()
        }
    }

}
