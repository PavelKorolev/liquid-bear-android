package com.pillowapps.liqear.callbacks.retrofit;

import com.pillowapps.liqear.entities.lastfm.LastfmResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class LastfmCallback<T extends LastfmResponse> implements Callback<T> {
    @Override
    public final void success(T data, Response response) {
        if (data.getErrorCode() == 0) {
            success(data);
        } else {
            failure(data.getMessage());
        }
    }

    @Override
    public void failure(RetrofitError error) {
        failure(error.getMessage());
    }

    public abstract void success(T data);

    public abstract void failure(String error);
}