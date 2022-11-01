package com.entertainment.basemvvmproject.base

import com.squareup.moshi.Json

data class ApiMemberResponse(
    @Json(name = "member")
    val member: MemberResponse?
) : BaseResponse()

data class MemberResponse(
    @Json(name = "age")
    val age: String = "",
    @Json(name = "area")
    val area: String = "",
    @Json(name = "comment")
    val comment: String = "",
    @Json(name = "isFavorite")
    val isFavorite: Boolean = false,
    @Json(name = "job")
    val job: String = "",
    @Json(name = "name")
    var name: String = "",
    @Json(name = "note")
    val note: String = "",
    @Json(name = "profileImageUrl")
    val profileImageUrl: String = "",

    var memberCode: String = "",
    var status: String = "",
    var isPeep: Boolean = false,
    var isNewMessage: Boolean = false,
)