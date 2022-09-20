package com.procharger.fastprocharrging.quickcharge.data.interactor

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.procharger.fastprocharrging.quickcharge.common.extension.appSettingsModel
import com.procharger.fastprocharrging.quickcharge.common.extension.logE
import com.procharger.fastprocharrging.quickcharge.common.util.CommonUtil
import com.procharger.fastprocharrging.quickcharge.common.util.RxBus

class IapInteractor(private val ctx: Context) : BaseInteractor() {

    companion object {
        private const val IAP_REMOVE_ADS = "remove_ads"
    }

    private val billingClient = BillingClient.newBuilder(ctx)
        .setListener { billingResult, purchases ->
            handlePurchase(billingResult, purchases)
        }
        .enablePendingPurchases()
        .build()

    fun checkVipStatus() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingSetUpResult: BillingResult) {
                if (billingSetUpResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchaseHistoryAsync(
                        BillingClient.SkuType.INAPP,
                        object : PurchaseHistoryResponseListener {
                            override fun onPurchaseHistoryResponse(
                                billingResult: BillingResult,
                                purchases: MutableList<PurchaseHistoryRecord>?
                            ) {
                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                                    val purchaseHistoryRecord =
                                        purchases.find { return@find it.sku == IAP_REMOVE_ADS }
                                    purchaseHistoryRecord?.let {
                                        ctx.logE("--- SKU history: ${purchaseHistoryRecord.sku}")
                                        ctx.appSettingsModel.apply {
                                            didRemoveAds = true
                                        }
                                    } ?: run {
                                        ctx.logE("--- SKU not payment: $IAP_REMOVE_ADS")
                                    }

                                    // Keep this to avoid checking many times
                                    ctx.appSettingsModel.apply {
                                        didCheckVipStatus = true
                                    }.run {
                                        CommonUtil.saveAppSettingsModel(ctx, this)
                                    }
                                } else {
                                    ctx.logE("--- SKU not payment: $IAP_REMOVE_ADS")
                                }
                            }

                        })
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    fun removeAds(activity: AppCompatActivity) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingSetUpResult: BillingResult) {
                if (billingSetUpResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    val skuList = ArrayList<String>()
                    skuList.add(IAP_REMOVE_ADS)

                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

                    billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                        // Process the result.
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                            && skuDetailsList != null
                        ) {
                            for (skuDetails in skuDetailsList) {
                                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                val flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                val responseCode = billingClient.launchBillingFlow(
                                    activity,
                                    flowParams
                                ).responseCode

                                if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                                    // Update app settings (mark user has removed ads)
                                    updateAppSettingsModel()
                                    break
                                }
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun handlePurchase(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.run {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    for (purchase in purchases) {
                        if (!purchase.isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                                if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                                    updateAppSettingsModel()
                                }
                            }
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    // Handle an error caused by a user cancelling the purchase flow.
                }
                else -> {
                    // Handle any other error codes.
                }
            }
        }
    }

    private fun updateAppSettingsModel() {
        ctx.appSettingsModel.apply {
            didRemoveAds = true
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)

            // Publish app settings changed event (ads removed)
            RxBus.publishAppSettingsModel(this)
        }
    }
}