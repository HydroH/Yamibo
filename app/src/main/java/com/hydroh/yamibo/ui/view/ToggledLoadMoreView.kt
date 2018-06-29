package com.hydroh.yamibo.ui.view

import com.chad.library.adapter.base.loadmore.LoadMoreView
import com.hydroh.yamibo.R

class ToggledLoadMoreView(private val endVisible: Boolean) : LoadMoreView() {

    override fun getLayoutId(): Int {
        return R.layout.quick_view_load_more
    }

    override fun getLoadingViewId(): Int {
        return R.id.load_more_loading_view
    }

    override fun getLoadFailViewId(): Int {
        return R.id.load_more_load_fail_view
    }

    override fun getLoadEndViewId(): Int {
        return if (endVisible) R.id.load_more_load_end_view else 0
    }
}