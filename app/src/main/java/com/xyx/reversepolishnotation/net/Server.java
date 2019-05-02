package com.xyx.reversepolishnotation.net;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Server {

    private final static String BASE_URL = "https://android-challenge.service.mport.cloud";

    private static Server mServer;

    public static synchronized Server getInstance() {
        if (mServer == null) {
            mServer = new Server();
        }
        return mServer;
    }

    private API mAPI;

    private Server() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mAPI = retrofit.create(API.class);
    }

    public Observable<TaskBean> getTask() {
        return mAPI.getTask();
    }
}
