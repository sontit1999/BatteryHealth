package com.sh.entertainment.fastcharge.ui.info

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import android.widget.TextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.extension.manufacturer
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.NumberUtil
import com.sh.entertainment.fastcharge.data.model.BatteryModel
import com.sh.entertainment.fastcharge.ui.base.BaseActivity
import com.sh.entertainment.fastcharge.ui.home.BatteryStatusReceiver
import kotlin.math.pow
import kotlin.math.sqrt

const val GIGABYTE = 1024 * 1024 * 1024f
const val MEGA_PIXEL = 1000 * 1000f

class InfoActivity : BaseActivity<InfoView, InfoPresenterImp>(), InfoView {

    private val lblHealth by lazy { findViewById<TextView>(R.id.lbl_health) }
    private val lblTechnology by lazy { findViewById<TextView>(R.id.lbl_technology) }
    private val lblTemperature by lazy { findViewById<TextView>(R.id.lbl_temperature) }
    private val lblCapacity by lazy { findViewById<TextView>(R.id.lbl_capacity) }
    private val lblModel by lazy { findViewById<TextView>(R.id.lbl_model) }
    private val lblRam by lazy { findViewById<TextView>(R.id.lbl_ram) }
    private val lblStorage by lazy { findViewById<TextView>(R.id.lbl_storage) }
    private val lblAndroidVersion by lazy { findViewById<TextView>(R.id.lbl_android_version) }
    private val lblScreenSize by lazy { findViewById<TextView>(R.id.lbl_screen_size) }
    private val lblScreenResolution by lazy { findViewById<TextView>(R.id.lbl_screen_resolution) }

    private val batteryStatusReceiver by lazy {
        BatteryStatusReceiver {
            fillBatteryInfo(it)
        }
    }

    override fun onDestroy() {
        // Unregister battery status receiver
        BatteryStatusReceiver.unregister(ctx, batteryStatusReceiver)

        super.onDestroy()
    }

    override fun initView(): InfoView {
        return this
    }

    override fun initPresenter(): InfoPresenterImp {
        return InfoPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_info
    }

    override fun onBackPressed() {
        showInter()
    }

    override fun initWidgets() {
        // Init toolbar
        showTitle(R.string.info)
        handleLoadInter()
        enableHomeAsUp {
            showInter()
        }

        // Listeners
        BatteryStatusReceiver.register(ctx, batteryStatusReceiver)

        // Fill device info
        fillDeviceInfo()
    }

    private fun fillBatteryInfo(model: BatteryModel) {
        with(model) {
            // Capacity
            lblCapacity.text =
                String.format(getString(R.string._mah), NumberUtil.formatNumber(capacity))

            // Battery temperature
            lblTemperature.text = String.format(
                getString(R.string._degree),
                NumberUtil.formatNumber(temperatureC, 1),
                NumberUtil.formatNumber(temperatureF, 1)
            )

            // Battery health
            lblHealth.text = when {
                isHealthCold -> getString(R.string.cold)
                isHealthDead -> getString(R.string.dead)
                isHealthGood -> getString(R.string.good)
                isHealthOverHeat -> getString(R.string.overheat)
                isHealthOverVoltage -> getString(R.string.over_voltage)
                isHealthUnspecifiedFailure -> getString(R.string.unspecified_failure)
                else -> getString(R.string.unknown)
            }

            // Battery technology
            lblTechnology.text = technology
        }
    }

    private fun fillDeviceInfo() {
        // CPU model
        lblModel.text = "$manufacturer - ${Build.MODEL}"

        // Fill RAM info
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem
        lblRam.text =
            String.format(getString(R.string._gb), NumberUtil.formatNumber(totalRam / GIGABYTE, 1))

        // Screen resolution info
        val screen = CommonUtil.getRealScreenSizeAsPixels(self)
        lblScreenResolution.text = String.format(
            getString(R.string._x_),
            NumberUtil.formatNumber(screen.x),
            NumberUtil.formatNumber(screen.y)
        )

        // Screen size info
        val dm = resources.displayMetrics
        val x = (screen.x / dm.xdpi).pow(2)
        val y = (screen.y / dm.ydpi).pow(2)
        val screenSize = sqrt(x + y)
        lblScreenSize.text =
            String.format(getString(R.string._inch), NumberUtil.formatNumber(screenSize, 1))

        // Storage info
        val internalStorage = StatFs(filesDir.absolutePath)
        val internalTotal =
            (internalStorage.blockSizeLong * internalStorage.blockCountLong) / GIGABYTE
        val internalAvailable =
            (internalStorage.blockSizeLong * internalStorage.availableBlocksLong) / GIGABYTE
        lblStorage.text = String.format(
            getString(R.string._gb_free),
            NumberUtil.formatNumber(internalAvailable, 1),
            NumberUtil.formatNumber(internalTotal, 1)
        )

        // Android version
        lblAndroidVersion.text = Build.VERSION.RELEASE

        // Camera info
        /*if (PermissionUtil.isApi21orHigher()) {
            val camManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            camManager?.run {
                for (cam in cameraIdList) {
                    logE("Cam id: $cam")
                    val streamConfigurationMap =
                        getCameraCharacteristics(cam).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    streamConfigurationMap?.run {
                        val pictureSizes = listOf(getOutputSizes(ImageFormat.YUV_420_888))
                        for (size in pictureSizes) {
                            logE("size: ${size.size}")
                            for (s in size) {
                                logE("Cam $cam: $s")
                            }
                        }

                    }
                }
            }
        }*/
    }

    private fun showInter() {
        if (MyApplication.interstitialAd == null || !MyApplication.remoteConfigModel.is_inter_back_info) {
            finish()
            return
        }
        if ((System.currentTimeMillis() - MyApplication.timeShowIntel) < MyApplication.remoteConfigModel.timeShowInter*1000) {
            finish()
            return
        }
        MyApplication.interstitialAd!!.fullScreenContentCallback = object :
            FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finish()
                MyApplication.timeShowIntel = System.currentTimeMillis()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                finish()
                MyApplication.interstitialAd = null
            }

            override fun onAdImpression() {}
            override fun onAdShowedFullScreenContent() {
            }
        }
        MyApplication.interstitialAd!!.show(this)
    }

    private fun handleLoadInter() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, MyApplication.KEY_INTEL, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    MyApplication.interstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    MyApplication.interstitialAd = null
                }
            })
    }

}