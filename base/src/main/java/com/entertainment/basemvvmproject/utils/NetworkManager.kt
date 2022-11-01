package com.entertainment.basemvvmproject.utils
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresApi

object NetworkManager {

    fun isOnline(context: Context?): Boolean {
        val connMgr =
            context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun handlerNetWorkState(context: Context?, callAvailable: () -> Unit, callLost: () -> Unit) {
        val connectivityManager =
            context?.applicationContext?.getSystemService(ConnectivityManager::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                //connect internet
                override fun onAvailable(network: Network) {
                    callAvailable.invoke()
                }

                // disconnect internet
                override fun onLost(network: Network) {
                    callLost.invoke()
                }

            })
        }
    }


}