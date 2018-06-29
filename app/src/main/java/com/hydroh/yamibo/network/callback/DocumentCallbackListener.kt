package com.hydroh.yamibo.network.callback

import com.hydroh.yamibo.util.DocumentParser

interface DocumentCallbackListener : ICallbackListener<DocumentParser> {
    override fun onFinish(docParser: DocumentParser)
    override fun onError(e: Exception)
}
