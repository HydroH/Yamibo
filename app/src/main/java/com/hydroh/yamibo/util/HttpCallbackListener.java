package com.hydroh.yamibo.util;

public interface HttpCallbackListener {
    void onFinish(DocumentParser doc);

    void onError(Exception e);
}
