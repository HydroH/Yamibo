package com.hydroh.yamibo.util

interface HttpCallbackListener {
    fun onFinish(docParser: DocumentParser)

    fun onError(e: Exception)
}
