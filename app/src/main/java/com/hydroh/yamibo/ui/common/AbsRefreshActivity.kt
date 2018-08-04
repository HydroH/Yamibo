package com.hydroh.yamibo.ui.common

import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.list_common.*

abstract class AbsRefreshActivity : AppCompatActivity() {
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