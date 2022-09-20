package com.procharger.fastprocharrging.quickcharge.data.mapper

import com.procharger.fastprocharrging.quickcharge.data.model.UserModel
import com.procharger.fastprocharrging.quickcharge.data.response.UserResponse

fun UserResponse.convertToModel(): UserModel {
    val res = this
    return UserModel(res.id ?: -1).apply {
        name = res.name
    }
}