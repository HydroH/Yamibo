package com.hydroh.yamibo.model

import java.util.*

class History<T>(var size: Int) {
    val mDataList = LinkedList<T>()

    fun update(data: T) {
        if (mDataList.contains(data)) {
            mDataList.remove(data)
        } else {
            if (mDataList.count() == size) {
                mDataList.removeLast()
            }
        }
        mDataList.addFirst(data)
    }

    fun clear() {
        mDataList.clear()
    }
}