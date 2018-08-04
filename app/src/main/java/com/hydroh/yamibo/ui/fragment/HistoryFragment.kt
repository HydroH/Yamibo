package com.hydroh.yamibo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hydroh.yamibo.R
import com.hydroh.yamibo.model.History
import com.hydroh.yamibo.model.Post
import com.hydroh.yamibo.ui.adapter.HistoryAdapter
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import com.hydroh.yamibo.util.PrefUtils
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.list_common.*
import org.jetbrains.anko.support.v4.ctx
import java.lang.RuntimeException

class HistoryFragment : Fragment() {

    private lateinit var mPostHistory: History<Post>
    private var mListener: HomeInteractListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListener?.onSetupToolbar(toolbar_history, "浏览历史")

        refresh_common.isEnabled = false
        mPostHistory = PrefUtils.getPostHistory(ctx)

        val layoutManager = LinearLayoutManager(ctx)
        list_common.layoutManager = layoutManager
        list_common.adapter = HistoryAdapter(mPostHistory.mDataList)

        val dividerItemDecoration =
                DividerItemDecoration(ctx, layoutManager.orientation).apply {
                    setDrawable(ContextCompat.getDrawable(ctx, R.drawable.divider_horizontal_thin)!!)
                }
        if (list_common.itemDecorationCount == 0) {
            list_common.addItemDecoration(dividerItemDecoration)
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
