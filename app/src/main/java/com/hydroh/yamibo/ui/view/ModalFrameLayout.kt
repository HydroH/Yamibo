package com.hydroh.yamibo.ui.view

import android.content.ContentValues.TAG
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout

class ModalFrameLayout : FrameLayout {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var isInterceptTouchEvent: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent: Intercepted touch event.")
        onTouchEvent(ev)
        return isInterceptTouchEvent
    }
}
