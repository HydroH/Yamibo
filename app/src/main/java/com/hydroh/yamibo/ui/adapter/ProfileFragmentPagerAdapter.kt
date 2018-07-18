package com.hydroh.yamibo.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import com.hydroh.yamibo.R
import com.hydroh.yamibo.ui.fragment.ProfileListFragment

class ProfileFragmentPagerAdapter(fragmentManager: android.support.v4.app.FragmentManager, private val mUid: String, private val mCount: Int, private val mContext: Context) : FragmentPagerAdapter(fragmentManager) {

    companion object {
        const val URL_TEMPLATE = "https://bbs.yamibo.com/home.php?mod=space&do=thread&view=me&type=%s&uid=%s&from=space"
    }

    private val mFragmentHashMap = SparseArray<Fragment>()

    override fun getItem(position: Int) = createFragment(position)

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> {
                mContext.resources.getString(R.string.profile_tab_post)
            }
            1 -> {
                mContext.resources.getString(R.string.profile_tab_reply)
            }
            else -> {
                throw NotImplementedError("Unimplemented tab type.")
            }
        }
    }

    override fun getCount() = mCount

    private fun createFragment(position: Int): Fragment {
        mFragmentHashMap.get(position, null)?.let {
            return it
        }

        return when (position) {
            0 -> {
                ProfileListFragment.newInstance(URL_TEMPLATE.format("thread", mUid))
            }
            1 -> {
                ProfileListFragment.newInstance(URL_TEMPLATE.format("reply", mUid))
            }
            else -> {
                throw NotImplementedError("Unimplemented tab type.")
            }
        }.also {
            mFragmentHashMap.put(position, it)
        }
    }
}