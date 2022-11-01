package com.entertainment.demoandroidrikkei.base.network

import okhttp3.*
import timber.log.Timber
import java.net.URI
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class WebSocketClient constructor(
    private val id: String,
    private val pass: String,
    private val webSocketInfo: WebSocketInfo,
    private val listener: OnReceiveMessageListener
) {
    private val TAG = "WebSocketClient"
    private lateinit var webSocket: WebSocket
    private var skipOnFail = false
    private var timeIntervalRetry = 1000L
    private var countIntervalRetry = 0
    private var maxIntervalRetry = 2

    fun connect() {
        Timber.tag(TAG).d("connecting")
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers: ${trustManagers.contentToString()}")
        }
        val trustManager: X509TrustManager = trustManagers[0] as X509TrustManager

        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .build()

        val wsUrl = getWebSocketUrl()
        val request: Request = Request.Builder()
            .url(wsUrl)
            .addHeader(SocketInfo.HEADER_ORIGIN, getOrigin(wsUrl))
            .build()

        webSocket = client.newWebSocket(request, getWebSocketListener())
    }

    private fun getWebSocketUrl(): String {
        val urlBuilder = StringBuilder()
        urlBuilder.append("wss://${webSocketInfo.url}/ws/performerLogin")
        urlBuilder.append("?${SocketInfo.ACTION}=${SocketInfo.ACTION_LOGIN}")
        urlBuilder.append("&${SocketInfo.AUTH_OWNER_CODE}=${webSocketInfo.ownerCode}")
        urlBuilder.append("&${SocketInfo.AUTH_ID}=${id}")
        urlBuilder.append("&${SocketInfo.AUTH_PASS}=${pass}")
        urlBuilder.append("&${SocketInfo.IS_USE_STREAM_TOKEN}=true")
        urlBuilder.append("&${SocketInfo.IS_IDLE}=false")
        urlBuilder.append("&${SocketInfo.WEB_CLIENT}=5")
        urlBuilder.append("&${SocketInfo.IS_ACL}=true")
        urlBuilder.append("&${SocketInfo.IS_LAS}=true")
        Timber.tag(TAG).d(urlBuilder.toString())
        return urlBuilder.toString()
    }

    private fun getOrigin(wsUrl: String): String {
        val uri = URI(wsUrl)
        return if (uri.scheme.equals("wss")) {
            "https://" + uri.authority
        } else {
            "http://" + uri.authority
        }
    }

    fun reconnect() {
        Thread.sleep(timeIntervalRetry)
        if (countIntervalRetry <= maxIntervalRetry) {
            countIntervalRetry++
            connect()
        }
    }

    fun sendMessage(jsonStr: String) {
        Timber.tag(TAG).d("sendMessage: $jsonStr.")
        webSocket.send(jsonStr)
    }

    private fun getWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Timber.tag(TAG).e("onClosed: $reason")
                listener.closedSocket(reason)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Timber.tag(TAG).e("onClosing: $reason")
                closeWebSocket("onClosing: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Timber.tag(TAG).e("onFailure: $t.")
                if (!skipOnFail) {
                    reconnect()
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Timber.tag(TAG).d(text)
                listener.handleMessage(text)
            }
        }
    }

    fun closeWebSocket(reason: String) {
        skipOnFail = true
        webSocket.close(1000, reason)
        Timber.tag(TAG).d("Client close socket")
    }

    interface OnReceiveMessageListener {
        fun handleMessage(message: String)

        fun closedSocket(reason: String)
    }

    object SocketInfo {
        // HEADER
        const val HEADER_ORIGIN = "origin"

        // Param
        const val ACTION = "action"
        const val AUTH_OWNER_CODE = "ownerCode"
        const val AUTH_ID = "id"
        const val AUTH_PASS = "pass"
        const val AUTH_TOKEN = "token"
        const val IS_USE_STREAM_TOKEN = "isUseStreamToken"
        const val IS_IDLE = "isIdle"
        const val WEB_CLIENT = "webClient"
        const val IS_ACL = "isACL"
        const val IS_LAS = "isLAS"
        const val LOGIN = "login"

        // Action
        const val ACTION_LOGIN = "Login"

    }

    class WebSocketInfo(
        val url: String,
        val ownerCode: String,
        val appKey: String,
    )
}