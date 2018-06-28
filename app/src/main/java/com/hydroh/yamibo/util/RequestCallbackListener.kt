package com.hydroh.yamibo.util

interface RequestCallbackListener {
    fun onFinish(cookies: Map<String, String>)

    fun onError(e: Exception)
}
