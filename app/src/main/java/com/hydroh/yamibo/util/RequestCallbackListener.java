package com.hydroh.yamibo.util;

import org.jsoup.nodes.Document;

import java.util.Map;

public interface RequestCallbackListener {
    void onFinish(Map<String, String> cookies);

    void onError(Exception e);
}
