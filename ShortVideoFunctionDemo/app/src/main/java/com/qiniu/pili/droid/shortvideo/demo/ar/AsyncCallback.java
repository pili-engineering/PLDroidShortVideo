package com.qiniu.pili.droid.shortvideo.demo.ar;

public interface AsyncCallback<T> {
    void onSuccess(T result);

    void onFail(Throwable t);

    void onProgress(String taskName, float progress);
}
