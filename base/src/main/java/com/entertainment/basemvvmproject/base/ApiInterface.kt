package com.entertainment.basemvvmproject.base

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import java.util.*

interface ApiInterface {
    @GET
    suspend fun getUserInfoByCode(
        @Url url: String,
        @Query("id") id: String,
        @Query("pass") pass: String,
        @Query("memberCode") memberCode: Int
    ): ApiMemberResponse

    // multi part
    @Multipart
    @POST("aiSample/upload")
    suspend fun uploadAISample(
        @Part("label") label: RequestBody,
        @Part("type") functionType: RequestBody,
        @Part image: MultipartBody.Part
    ): Objects

    @POST("aiSample/get")
    suspend fun getAISampleList(@Body body: MemberResponse): Objects

    @Multipart
    @POST("endpoint")
    fun createNewFanClub(
        @Part("category_id") category_id: RequestBody?,
        @Part("store_id") store_id: RequestBody?,
        @Part("rating") rating: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part files: Array<Part?>?
    ): Observable<BaseObjectLifeStyleModel<Objects>?>?


}