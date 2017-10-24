package com.hydroh.yamibo.util;

import org.jsoup.nodes.Document;

public interface HttpCallbackListener {
    void onFinish(Document doc);

    void onError(Exception e);
}
