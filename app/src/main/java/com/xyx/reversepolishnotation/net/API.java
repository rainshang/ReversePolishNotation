package com.xyx.reversepolishnotation.net;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface API {

    @GET("/api/tasks")
    Observable<TaskBean> getTask();
}
