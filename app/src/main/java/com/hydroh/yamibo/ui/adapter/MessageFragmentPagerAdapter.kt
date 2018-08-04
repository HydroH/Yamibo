package com.hydroh.yamibo.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.fragment.MessageMailFragment
import com.hydroh.yamibo.ui.fragment.MessageReplyFragment

class MessageFragmentPagerAdapter(fragmentManager: android.support.v4.app.FragmentManager, private val mCount: Int, private val mContext: Context) : FragmentPagerAdapter(fragmentManager) {

    private val mFragmentHashMap = SparseArray<Fragment>()

    override fun getItem(position: Int) = createFragment(position)

    override fun getPageTitle(position: Int): CharSequence? {
        return mContext.resources.run {
            when (position) {
                0 -> getString(R.string.message_tab_mail)
                1 -> getString(R.string.message_tab_reply)
                else -> throw NotImplementedError("Unimplemented tab type.")
            }
        }
    }

    override fun getCount() = mCount

    private fun createFragment(position: Int): Fragment {
        mFragmentHashMap.get(position, null)?.let {
            return it
        }

        return when (position) {
            0 -> MessageMailFragment()
            1 -> MessageReplyFragment()
            else -> throw NotImplementedError("Unimplemented tab type.")
        }.also {
            mFragmentHashMap.put(position, it)
        }
    }
}