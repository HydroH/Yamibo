package com.hydroh.yamibo.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.common.AbsRefreshListFragment

class MessageMailFragment : AbsRefreshListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_message_mail, container, false)

    override fun loadContent() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
