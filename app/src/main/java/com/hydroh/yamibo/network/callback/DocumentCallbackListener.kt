package com.hydroh.yamibo.network.callback

import com.hydroh.yamibo.util.DocumentParser

interface DocumentCallbackListener {
    fun onFinish(docParser: DocumentParser)
    fun onError(e: Exception)
}
