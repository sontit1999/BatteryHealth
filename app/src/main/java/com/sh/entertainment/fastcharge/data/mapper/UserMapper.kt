package com.sh.entertainment.fastcharge.data.mapper

import com.sh.entertainment.fastcharge.data.model.UserModel
import com.sh.entertainment.fastcharge.data.response.UserResponse

fun UserResponse.convertToModel(): UserModel {
    val res = this
    return UserModel(res.id ?: -1).apply {
        name = res.name
    }
}