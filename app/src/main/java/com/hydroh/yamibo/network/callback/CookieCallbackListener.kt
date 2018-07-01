package com.hydroh.yamibo.network.callback

interface CookieCallbackListener {
    fun onFinish(cookies: MutableMap<String, String>)
    fun onError(e: Exception)
}
