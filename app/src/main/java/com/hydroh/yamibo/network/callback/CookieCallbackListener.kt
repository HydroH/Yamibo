package com.hydroh.yamibo.network.callback

interface CookieCallbackListener : ICallbackListener<MutableMap<String, String>> {
    override fun onFinish(cookies: MutableMap<String, String>)
    override fun onError(e: Exception)
}
