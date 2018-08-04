package com.hydroh.yamibo.ui.common

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.hydroh.yamibo.R
import kotlinx.android.synthetic.main.list_common.*

abstract class AbsRefreshListFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh_common.setOnRefreshListener {
            refreshList(refresh_common)
        }
        hint_text.setOnClickListener {
            refreshList(hint_text)
        }
        refreshList(hint_text)
    }

    protected fun refreshList(view: View) {
        if (view.id == R.id.hint_text) {
            setRefreshState(RefreshState.START_MAIN)
        } else if (view.id == R.id.refresh_common) {
            setRefreshState(RefreshState.START_SWIPE)
        }
        loadContent()
    }

    protected abstract fun loadContent()

    protected fun setRefreshState(state: RefreshState) {
        when (state) {
            RefreshState.START_MAIN -> {
                refresh_common.isRefreshing = false
                hint_text.visibility = View.GONE
                hint_progressbar.visibility = View.VISIBLE
            }
            RefreshState.START_SWIPE -> {
                refresh_common.isRefreshing = true
                hint_text.visibility = View.GONE
                hint_progressbar.visibility = View.GONE
            }
            RefreshState.FINISH -> {
                refresh_common.isRefreshing = false
                hint_text.visibility = View.GONE
                hint_progressbar.visibility = View.GONE
            }
            RefreshState.ERROR -> {
                refresh_common.isRefreshing = false
                hint_text.visibility = View.VISIBLE
                hint_progressbar.visibility = View.GONE
            }
        }
    }
}