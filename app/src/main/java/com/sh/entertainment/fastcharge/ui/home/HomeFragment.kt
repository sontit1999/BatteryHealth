package com.sh.entertainment.fastcharge.ui.home

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.NumberUtil
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import com.sh.entertainment.fastcharge.data.model.BatteryModel
import com.sh.entertainment.fastcharge.databinding.FragmentHomeBinding
import com.sh.entertainment.fastcharge.ui.base.BaseFragment
import com.sh.entertainment.fastcharge.ui.battery.BatteryActivity
import com.sh.entertainment.fastcharge.ui.booster.BoosterActivity
import com.sh.entertainment.fastcharge.ui.cool.CoolerActivity
import com.sh.entertainment.fastcharge.ui.info.GIGABYTE
import com.sh.entertainment.fastcharge.ui.optimize.OptimizeActivity
import kotlin.math.pow
import kotlin.math.sqrt


class HomeFragment : BaseFragment<FragmentHomeBinding, HomeView, HomePresenterImp>(), HomeView {

    private lateinit var scrollView: NestedScrollView
    private lateinit var btnOptimize: TextView
    private lateinit var lblOptimizationDescProcess: TextView
    private lateinit var lblWarnUsbCharging: TextView
    private lateinit var lblPercentage: TextView
    private lateinit var txtInfo: TextView
    private lateinit var txtMin: TextView
    private lateinit var txtHours: TextView
    private lateinit var viewTimeLeft: LinearLayout

    private var currentPercentage: Float? = null
    private var isCharging = false
    private var isJustOpenedApp = true // Always allow user to optimize when open app
    private var percentageCurrent = 0

    private val batteryInfoReceiver by lazy {
        BatteryStatusReceiver {
            fillBatteryInfo(it)
        }
    }

    private val batteryStatusReceiver by lazy {
        BatteryStatusReceiver {
            isCharging = it.isCharging
            currentPercentage = it.percentage

            fillBatteryInfo(it)
            updateOptimizeButton()

            if (isCharging) {
                if (MyApplication.didOptimized) {
                    txtInfo.visible()
                    viewTimeLeft.visible()
                    updateTextInfo(it)
                } else {
                    txtInfo.gone()
                    viewTimeLeft.gone()
                }
            } else {
                txtInfo.visible()
                viewTimeLeft.visible()
                updateTextInfo(it)
            }
            // Update "Optimize button" text
            if (!MyApplication.didOptimized) {
                if (currentPercentage != 100f) {
                    btnOptimize.text = getString(R.string.optimize)
                } else {
                    btnOptimize.text = getString(R.string.full)
                    btnOptimize.apply {
                        setBackgroundResource(R.drawable.btn_green)
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        fillDeviceInfo()
        BatteryStatusReceiver.register(ctx, batteryInfoReceiver)
        // Check if #forceOptimize is true then call #startOptimizing() method immediately
        val forceOptimize = arguments?.getBoolean(ARG_FORCE_OPTIMIZE) ?: false
        if (forceOptimize) {
            startOptimizing(true)
        }
        binding.bgCooler.setOnSafeClickListener {
            openActivity(CoolerActivity::class.java)
        }
        binding.bgBooster.setOnSafeClickListener {
            openActivity(BoosterActivity::class.java)
        }
        binding.bgBatterySaved.setOnSafeClickListener {
            openActivity(BatteryActivity::class.java)
        }
    }

    private fun checkPermission() {
        if (!requireContext().canWriteSettings()) {
            requireContext().showDrawOverlayPermissionDescDialog(onOkListener = {
                if (!requireContext().canWriteSettings()) requireContext().requestWriteSettingsPermission(
                    self,
                    RC_WRITE_SETTINGS
                )

            }, onCancelListener = {

            })
        }

    }

    private fun checkCanOverlayPermission() {
        if (!requireContext().canDrawOverlay()) {
            requireContext().showDrawOverlayPermissionDescDialog(onOkListener = {
                requireContext().requestDrawOverlayPermission(
                    self,
                    RC_DRAW_OVERLAY
                )
            }, onCancelListener = {

            })
        }
    }

    override fun onResume() {
        super.onResume()

        updateOptimizeButton()
        // Check if device is needed to optimize or not
        ctx?.run {
            if (!isXiaomiDevice) {
                refreshOptimizationUIState()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_WRITE_SETTINGS && ctx?.canWriteSettings() == true) {
            checkCanOverlayPermission()
        } else if (requestCode == RC_DRAW_OVERLAY && ctx?.canDrawOverlay() == true) {

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        // Unregister battery status receiver
        BatteryStatusReceiver.unregister(ctx, batteryStatusReceiver)
        super.onDestroyView()
    }

    override fun initView(): HomeView {
        return this
    }

    override fun initPresenter(): HomePresenterImp {
        return HomePresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            scrollView = findViewById(R.id.scrollview)
            btnOptimize = findViewById(R.id.btn_optimize)
            lblWarnUsbCharging = findViewById(R.id.lbl_warn_usb_charging)
            lblPercentage = findViewById(R.id.lbl_percentage)
            viewTimeLeft = findViewById(R.id.view_time_left)
            txtInfo = findViewById(R.id.txtInfo)
            txtMin = findViewById(R.id.tvMin)
            txtHours = findViewById(R.id.tvHour)
        }

        // Disable scroll content if ads is disabled
        ctx?.run {
            if (!shouldShowAds() || !adsConfigModel.isBannerHomeEnabled) {
                scrollView.isNestedScrollingEnabled = false
            }
        }

        // Listeners
        presenter.listenAppSettingsChanged()
        BatteryStatusReceiver.register(ctx, batteryStatusReceiver)

        try {
            if (MyApplication.remoteConfigModel.is_native_home) {
                //  nativeAdView.showAd(adRequest, MyApplication.KEY_NATIVE)
            }
            Log.d("QuangTB", "try")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("QuangTB", "catch")
        }

        btnOptimize.setOnSafeClickListener {
            startOptimizing()
        }
        // Fill UI
        updateOptimizeButton()

        loadNativeAds()
    }

    private fun loadNativeAds() {
        AdsManager.showNativeAd(requireContext(), binding.nativeAdView, AdsManager.NATIVE_AD_KEY)
    }


    override fun hasOptimizationOptionsSelected(): Boolean {
        return ctx?.let {
            it.appSettingsModel.isClearRam || it.appSettingsModel.isTurnOffAutoSync || it.appSettingsModel.isTurnOffBluetooth
                    || it.appSettingsModel.isTurnOffScreenRotation || (!PermissionUtil.isApi29orHigher() && it.appSettingsModel.isTurnOffWifi)
        } ?: false
    }

    override fun onNoOptimizationOptionsSelectedError() {
        ctx?.toast(R.string.no_optimization_options_selected, length = Toast.LENGTH_LONG)
    }


    override fun onOptimizing() {
    }

    override fun onDestroy() {
        super.onDestroy()
        BatteryStatusReceiver.unregister(ctx, batteryInfoReceiver)
    }

    override fun onOptimizationSuccess() {
    }

    override fun onAppSettingsChanged(model: AppSettingsModel) {
    }

    override fun showOptimizationDescDialog() {
        val dialog = ctx?.let { Dialog(it) }
        dialog?.run {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(R.color.transparent)
            setContentView(R.layout.dialog_optimization_desc)
            setCancelable(false)

            val lblWifi = findViewById<TextView>(R.id.lbl_wifi)
            val btnReject = findViewById<TextView>(R.id.btn_reject)
            val btnAllow = findViewById<TextView>(R.id.btn_allow)

            if (PermissionUtil.isApi29orHigher()) {
                lblWifi.gone()
            }

            btnReject.setOnSafeClickListener {
                // Dismiss the dialog
                if (isShowing) {
                    dismiss()
                }
            }

            btnAllow.setOnSafeClickListener {
                // Update flag to settings
                ctx?.appSettingsModel?.apply {
                    didShowOptimizationDescDialog = true
                }?.run {
                    CommonUtil.saveAppSettingsModel(ctx, this)
                }

                // Continue optimizing
                startOptimizing()

                // Dismiss the dialog
                if (isShowing) {
                    dismiss()
                }
            }

            if (!isShowing) {
                show()
            }
        }
    }

    override fun requestWriteSettingsPermission() {
        ctx?.requestWriteSettingsPermission(self, RC_WRITE_SETTINGS)
    }

    fun startOptimizing(forceOptimize: Boolean = false) {
        if (!MyApplication.didOptimized || forceOptimize) {
            openActivity(OptimizeActivity(isCharging)::class.java)
            if (currentPercentage != 100f && ctx?.appSettingsModel?.batteryPercentage != 100f) {
                updateBrightness()
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.your_device_ready_for_quick_charge),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateBrightness() {
        if (requireContext().canWriteSettings() && requireContext().appSettingsModel.isReduceScreenTimeOut) {
            try {
                Settings.System.putInt(
                    requireContext().contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                val brightness = Settings.System.getInt(
                    requireContext().contentResolver,
                    SCREEN_BRIGHTNESS
                )
                MyApplication.brightnessValue = brightness
                if (brightness < 40) return
                Settings.System.putInt(requireContext().contentResolver, SCREEN_BRIGHTNESS, 40)
                requireActivity().window.changeAppScreenBrightnessValue(40F)

            } catch (e: SettingNotFoundException) {
                Log.e("Error", "Cannot access system brightness")
                e.printStackTrace()
            }
        }
    }

    private fun Window.changeAppScreenBrightnessValue(brightnessValue: Float) {
        val layoutParams = this.attributes
        layoutParams.screenBrightness = brightnessValue
        this.attributes = layoutParams
    }

    fun refreshOptimizationUIState() {
//        Log.d("HaiHT", "refreshOptimizationUIState")
//        if (didOptimize) {
//            if (shouldOptimize()) {
//                didOptimize = false
//                updateOptimizeButton()
//            }
//        } else {
//            if (!shouldOptimize()) {
//                didOptimize = true
//                updateOptimizeButton()
//            }
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillDeviceInfo() {
        // CPU model
        binding.lblModel.text = "${requireContext().manufacturer} - ${Build.MODEL}"

        // Fill RAM info
        val activityManager =
            requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem
        binding.lblRam.text =
            String.format(getString(R.string._gb), NumberUtil.formatNumber(totalRam / GIGABYTE, 1))

        // Screen resolution info
        val screen = CommonUtil.getRealScreenSizeAsPixels(requireActivity() as AppCompatActivity)
        binding.lblScreenResolution.text = String.format(
            getString(R.string._x_),
            NumberUtil.formatNumber(screen.x),
            NumberUtil.formatNumber(screen.y)
        )

        // Screen size info
        val dm = resources.displayMetrics
        val x = (screen.x / dm.xdpi).pow(2)
        val y = (screen.y / dm.ydpi).pow(2)
        val screenSize = sqrt(x + y)
        binding.lblScreenSize.text =
            String.format(getString(R.string._inch), NumberUtil.formatNumber(screenSize, 1))

        // Storage info
        val internalStorage = StatFs(requireActivity().filesDir.absolutePath)
        val internalTotal =
            (internalStorage.blockSizeLong * internalStorage.blockCountLong) / GIGABYTE
        val internalAvailable =
            (internalStorage.blockSizeLong * internalStorage.availableBlocksLong) / GIGABYTE
        binding.lblStorage.text = String.format(
            getString(R.string._gb_free),
            NumberUtil.formatNumber(internalAvailable, 1),
            NumberUtil.formatNumber(internalTotal, 1)
        )

        // Android version
        binding.lblAndroidVersion.text = Build.VERSION.RELEASE
    }

    private fun fillBatteryInfo(model: BatteryModel) {
        with(model) {
            // Battery percentage
            lblPercentage.text =
                String.format(getString(R.string._percentage), NumberUtil.formatNumber(percentage))
            // Capacity
            binding.lblCapacity.text =
                String.format(getString(R.string._mah), NumberUtil.formatNumber(capacity))

            // Battery temperature
            binding.lblTemperature.text = String.format(
                getString(R.string._degree),
                NumberUtil.formatNumber(temperatureC, 1),
                NumberUtil.formatNumber(temperatureF, 1)
            )

            // Battery health
            binding.lblHealth.text = when {
                isHealthCold -> getString(R.string.cold)
                isHealthDead -> getString(R.string.dead)
                isHealthGood -> getString(R.string.good)
                isHealthOverHeat -> getString(R.string.overheat)
                isHealthOverVoltage -> getString(R.string.over_voltage)
                isHealthUnspecifiedFailure -> getString(R.string.unspecified_failure)
                else -> getString(R.string.unknown)
            }

            // Battery technology
            binding.lblTechnology.text = technology

            // Percentage effect
            percentage?.toInt()?.run {
                if (percentageCurrent != this) {
                    percentageCurrent = this
                }
            }
        }
    }

    private fun updateTextInfo(model: BatteryModel) {
        if (model.isCharging) {
            val plugged = getPlugged(requireContext())
            val usbCharge = plugged == BatteryManager.BATTERY_PLUGGED_USB
            val time: Int = if (usbCharge) {
                lblWarnUsbCharging.visible()
                BatteryPref.initilaze(context)!!
                    .getTimeChargingUsb(requireContext(), getBatteryLevel(requireContext()))
            } else {
                lblWarnUsbCharging.gone()
                BatteryPref.initilaze(context)!!
                    .getTimeChargingAc(requireContext(), getBatteryLevel(requireContext()))
            }
            txtInfo.text = getString(R.string.time_charging_left)
            txtHours.text = (time / 60).toString()
            txtMin.text = (time % 60).toString()
        } else {
            val time = BatteryPref.initilaze(context)!!
                .getTimeRemainning(requireContext(), getBatteryLevel(requireContext()))
            txtInfo.text = getString(R.string.time_using_left)
            txtHours.text = (time / 60).toString()
            txtMin.text = (time % 60).toString()
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        return try {
            val registerReceiver = context.applicationContext.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
            return registerReceiver!!.getIntExtra("level", -1)
        } catch (unused: java.lang.Exception) {
            -1
        }
    }

    private fun getPlugged(context: Context): Int {
        return try {
            val registerReceiver = context.applicationContext.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
            return registerReceiver!!.getIntExtra("plugged", -1)
        } catch (unused: java.lang.Exception) {
            -1
        }
    }

    private fun updateOptimizeButton() {
        if (MyApplication.didOptimized) {
            btnOptimize.apply {
                isEnabled = true
                text = getString(R.string.optimized)
                setBackgroundResource(R.drawable.btn_green)
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tick_circle, 0, 0, 0)
            }
        } else {
            btnOptimize.apply {
                text = getString(R.string.optimize)
                setBackgroundResource(R.drawable.btn_yellow)
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

    private fun shouldOptimize(): Boolean {
        return ctx?.let { ctx ->
            (ctx.appSettingsModel.isTurnOffBluetooth && ctx.isBluetoothEnabled) ||
                    (ctx.appSettingsModel.isTurnOffAutoSync && ctx.isAutoSyncEnabled) ||
                    (ctx.appSettingsModel.isTurnOffScreenRotation && ctx.isAutoRotationEnabled) ||
                    isJustOpenedApp
        } ?: false
    }


    override fun getLayoutID(): Int {
        return R.layout.fragment_home
    }

    companion object {
        private const val ARG_FORCE_OPTIMIZE = "arg_force_optimize"

        private const val RC_WRITE_SETTINGS = 256
        private const val RC_DRAW_OVERLAY = 257

        fun newInstance(forceOptimize: Boolean): HomeFragment {
            val fragment = HomeFragment()
            bundleOf(ARG_FORCE_OPTIMIZE to forceOptimize).apply {
                fragment.arguments = this
            }
            return fragment
        }
    }
}