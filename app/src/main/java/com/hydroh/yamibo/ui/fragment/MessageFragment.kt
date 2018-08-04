package com.hydroh.yamibo.ui.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.adapter.MessageFragmentPagerAdapter
import com.hydroh.yamibo.ui.fragment.listener.HomeInteractListener
import kotlinx.android.synthetic.main.fragment_message.*
import org.jetbrains.anko.support.v4.ctx
import java.lang.RuntimeException

class MessageFragment : Fragment() {

    private var mListener: HomeInteractListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_message, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListener?.onSetupToolbar(toolbar_message, "消息")

        pager_tab_message.adapter = MessageFragmentPagerAdapter(childFragmentManager, tab_message.tabCount, ctx)
        tab_message.setupWithViewPager(pager_tab_message)
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
