package com.hydroh.yamibo.ui.fragment.listener

import android.support.v7.widget.Toolbar

interface HomeInteractListener {
    fun onHomeRefresh()
    fun onToolbarReady(toolbar: Toolbar)
    fun onUserStatReady(isLoggedIn: Boolean, avatarUrl: String?, username: String?, uid: String?)
}