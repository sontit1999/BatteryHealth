package com.entertainment.basemvvmproject.base;

import com.google.gson.annotations.SerializedName;

public class BaseObjectLifeStyleModel<D>{
    @SerializedName("data")
    private D data;

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
