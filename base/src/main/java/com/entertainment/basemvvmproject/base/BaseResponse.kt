package com.entertainment.basemvvmproject.base

import com.squareup.moshi.Json

open class BaseResponse {
    @Json(name = "isSuccess")
    open var isSuccess: Boolean = false

    @Json(name = "errorMessages")
    open var errorMessages: List<String>? = ArrayList()

    fun getErrorMessage(): String {
        return if (errorMessages == null || errorMessages!!.isEmpty()) {
            ""
        } else {
            errorMessages!![0]
        }

    }
}