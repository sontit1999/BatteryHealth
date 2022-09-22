package com.sh.entertainment.fastcharge.data.response

import com.google.gson.annotations.SerializedName

class UserResponse(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null
) : BaseResponse<UserResponse>()