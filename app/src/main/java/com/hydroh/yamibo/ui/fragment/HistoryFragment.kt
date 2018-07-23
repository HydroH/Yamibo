package com.hydroh.yamibo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hydroh.yamibo.R
import com.hydroh.yamibo.model.History
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.ui.adapter.HistoryAdapter
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.PrefUtils
import java.lang.RuntimeException

class HistoryFragment : Fragment() {

    private lateinit var mPostHistory: History<Post>
    private var mListener: HomeInteractListener? = null

    private val mToolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar_history) }
    private val mSwipeRefreshLayout by lazy { view!!.findViewById<SwipeRefreshLayout>(R.id.refresh_common) }
    private val mContentRecyclerView by lazy { view!!.findViewById<RecyclerView>(R.id.list_common) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.run {
            if (this is AppCompatActivity) {
                setSupportActionBar(mToolbar)
                supportActionBar?.title = "浏览历史"
            }
        }
        mListener?.onToolbarReady(mToolbar)

        mSwipeRefreshLayout.isEnabled = false
        mPostHistory = PrefUtils.getPostHistory(activity!!)

        val layoutManager = LinearLayoutManager(mContentRecyclerView.context)
        mContentRecyclerView.layoutManager = layoutManager

        val adapter = HistoryAdapter(mPostHistory.mDataList)
        mContentRecyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
                mContentRecyclerView.context,
                layoutManager.orientation
        )
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(mContentRecyclerView.context, R.drawable.divider_horizontal_thin)!!)
        if (mContentRecyclerView.itemDecorationCount == 0) {
            mContentRecyclerView.addItemDecoration(dividerItemDecoration)
        }
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
