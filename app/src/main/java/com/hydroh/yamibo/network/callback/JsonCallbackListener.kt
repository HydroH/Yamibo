package com.hydroh.yamibo.network.callback

import org.json.JSONObject

interface JsonCallbackListener {
    fun onFinish(jsonObject: JSONObject)
    fun onError(e: Exception)
}