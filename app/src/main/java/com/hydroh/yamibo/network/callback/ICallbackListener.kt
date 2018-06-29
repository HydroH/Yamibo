package com.hydroh.yamibo.network.callback

interface ICallbackListener<T> {
    fun onFinish(result: T)
    fun onError(e: Exception)
}
