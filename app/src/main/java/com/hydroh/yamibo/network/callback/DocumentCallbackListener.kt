package com.hydroh.yamibo.network.callback

import org.jsoup.nodes.Document

interface DocumentCallbackListener {
    fun onFinish(document: Document)
    fun onError(e: Exception)
}
