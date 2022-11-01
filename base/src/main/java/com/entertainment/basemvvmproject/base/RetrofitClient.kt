package com.entertainment.basemvvmproject.base

import android.text.TextUtils
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val PREF_FILE_NAME = "Preferences"
const val DEFAULT_TIMEOUT = 30
const val DURATION_TIME_CLICKABLE = 500
const val COLOR_WARNING = "#EE2324"
const val TIME_ASK_TWO_SHOT_AUTO_DISMISS = 30000L
const val STREAM_BLUETOOTH_HEADPHONE = 6
const val BASE_URL = "BASE_URL"
const val REQUEST_CODE_200 = 200    //normal
const val REQUEST_CODE_400 = 400    //parameter error
const val REQUEST_CODE_401 = 401    //unauthorized error
const val REQUEST_CODE_403 = 403
const val REQUEST_CODE_404 = 404    //No data error
const val REQUEST_CODE_500 = 500    //system error

class RetrofitClient {
    fun getApiInterface(): ApiInterface {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(provideHttpClient(null, "Token"))
            .build()
        return retrofit.create(ApiInterface::class.java)
    }

    private fun provideHttpClient(cache: Cache?, token: String): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = if (!TextUtils.isEmpty(token)) {
                    chain.request()
                        .newBuilder()
                        .header("Content-Type", "application/json")
                        .addHeader(
                            "Authorization", token
                        )
                        .build()
                } else {
                    chain.request()
                        .newBuilder()
                        .header("Content-Type", "application/json")
                        .build()
                }

                chain.proceed(request)
            })
            .addInterceptor(loggingInterceptor)
            .addInterceptor(NetworkInterceptor())
            .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
    }
}

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            REQUEST_CODE_400, REQUEST_CODE_401, REQUEST_CODE_403, REQUEST_CODE_404 -> {
                Timber.d("network error code ${response.code}")
            }
        }
        return response
    }
}