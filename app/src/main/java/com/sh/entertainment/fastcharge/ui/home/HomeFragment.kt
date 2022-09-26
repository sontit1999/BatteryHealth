package com.sh.entertainment.fastcharge.ui.home

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.*
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieAnimationView
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.*
import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import com.sh.entertainment.fastcharge.data.model.BatteryModel
import com.sh.entertainment.fastcharge.data.model.TaskInfo
import com.sh.entertainment.fastcharge.databinding.FragmentHomeBinding
import com.sh.entertainment.fastcharge.ui.base.BaseFragment
import com.sh.entertainment.fastcharge.ui.booster.BoosterActivity
import com.sh.entertainment.fastcharge.ui.boresult.OptimizationResultActivity
import com.sh.entertainment.fastcharge.ui.cool.CoolerActivity
import com.sh.entertainment.fastcharge.ui.info.GIGABYTE
import com.sh.entertainment.fastcharge.widget.ads.LayoutNativeAd
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class HomeFragment : BaseFragment<FragmentHomeBinding, HomeView, HomePresenterImp>(), HomeView {

    private lateinit var scrollView: NestedScrollView
    private lateinit var animViewOptimization: LottieAnimationView
    private lateinit var btnOptimize: TextView
    private lateinit var lblOptimizationDescProcess: TextView
    private lateinit var lblWarnUsbCharging: TextView
    private lateinit var lblPercentage: TextView
    private lateinit var nativeAdView: LayoutNativeAd

    private lateinit var txtInfo: TextView
    private lateinit var txtMin: TextView
    private lateinit var txtHours: TextView
    private lateinit var viewTimeLeft: LinearLayout

    private var arrGravity1: IntArray = intArrayOf(49, 19, 83)
    private var arrGravity2: IntArray = intArrayOf(51, 49, 21)
    private var arrGravity3: IntArray = intArrayOf(53, 21, 81)
    private var arrGravity4: IntArray = intArrayOf(85, 81, 19)

    private var arrGravitys: Array<IntArray> =
        arrayOf(arrGravity1, arrGravity2, arrGravity3, arrGravity4)
    var curIndex = 0
    private var mPackageManager: PackageManager? = null
    private var mActivityManager: ActivityManager? = null
    private var currentPercentage: Float? = null
    private var isCharging = false
    private var didOptimize = false
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

            if (isCharging) {
                if (didOptimize) {
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
            if (!didOptimize) {

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
    }

    private fun checkPermission() {
        if (!requireContext().canDrawOverlay()) {
            requireContext().showDrawOverlayPermissionDescDialog(onOkListener = {
                requireContext().requestDrawOverlayPermission(
                    self,
                    OptimizationResultActivity.RC_DRAW_OVERLAY
                )
            }, onCancelListener = {

            })
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if device is needed to optimize or not
        ctx?.run {
            if (!isXiaomiDevice) {
                refreshOptimizationUIState()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_WRITE_SETTINGS && ctx?.canWriteSettings() == true) {
            startOptimizing()
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
            lblOptimizationDescProcess = findViewById(R.id.lbl_optimization_desc_process)
            lblWarnUsbCharging = findViewById(R.id.lbl_warn_usb_charging)
            lblPercentage = findViewById(R.id.lbl_percentage)
            // nativeAdView = findViewById(R.id.nativeAdView)
            viewTimeLeft = findViewById(R.id.view_time_left)
            txtInfo = findViewById(R.id.txtInfo)
            txtMin = findViewById(R.id.tvMin)
            txtHours = findViewById(R.id.tvHour)
        }

        LoadRunningTask(this).execute(*arrayOfNulls(0))

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
        /* if(MyApplication.remoteConfigModel.is_native_home){
             AdsManager.showNativeAd(requireContext(),binding.nativeAdView,AdsManager.NATIVE_AD_KEY)
         }*/
        AdsManager.showNativeAd(requireContext(), binding.nativeAdView, AdsManager.NATIVE_AD_KEY)
    }

    @Deprecated("Deprecated in Java")
    private class LoadRunningTask(var homeFragment: HomeFragment) :
        AsyncTask<Void, Drawable, Void>() {
        val activity = homeFragment.requireActivity()

        @SuppressLint("QueryPermissionsNeeded")
        override fun doInBackground(vararg voidArr: Void): Nothing? {
            val activityManager =
                activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningAppProcesses = activityManager.runningAppProcesses
            ArrayList<Any?>()
            return when {
                Build.VERSION.SDK_INT <= 21 -> {
                    for (runningAppProcessInfo in runningAppProcesses) {
                        try {
                            if (homeFragment.mPackageManager == null) {
                                return null
                            }
                            val str = runningAppProcessInfo.processName
                            val applicationInfo =
                                homeFragment.mPackageManager!!.getApplicationInfo(str, 0)
                            if (!str.contains(activity.packageName)
                                && Utils.isUserApp(applicationInfo) && !Utils.checkLockedItem(
                                    activity,
                                    str
                                )
                            ) {
                                val taskInfo = TaskInfo(activity, applicationInfo)
                                homeFragment.mActivityManager!!.killBackgroundProcesses(taskInfo.appinfo.packageName)
                                val applicationIcon =
                                    activity.packageManager.getApplicationIcon(
                                        taskInfo.appinfo.packageName ?: ""
                                    )
                                publishProgress(*arrayOf(applicationIcon))
                                try {
                                    Thread.sleep(150)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (unused: java.lang.Exception) {
                            Log.e("fff", "")
                        }
                    }
                    null
                }
                Build.VERSION.SDK_INT < 26 -> {
                    for (next in activityManager.getRunningServices(Int.MAX_VALUE)) {
                        try {
                            if (homeFragment.mPackageManager == null) {
                                return null
                            }
                            val packageInfo = homeFragment.mPackageManager!!.getPackageInfo(
                                next.service.packageName,
                                PackageManager.GET_ACTIVITIES
                            )

                            if (packageInfo != null) {
                                val applicationInfo2 =
                                    homeFragment.mPackageManager!!.getApplicationInfo(
                                        packageInfo.packageName,
                                        0
                                    )
                                if (!packageInfo.packageName.contains(activity.packageName)
                                    && Utils.isUserApp(applicationInfo2)
                                    && !Utils.checkLockedItem(activity, packageInfo.packageName)
                                ) {
                                    val taskInfo2 = TaskInfo(activity, applicationInfo2)
                                    homeFragment.mActivityManager!!.killBackgroundProcesses(
                                        taskInfo2.appinfo.packageName
                                    )
                                    val applicationIcon2 =
                                        activity.packageManager.getApplicationIcon(
                                            taskInfo2.appinfo.packageName ?: ""
                                        )
                                    publishProgress(*arrayOf(applicationIcon2))
                                    try {
                                        Thread.sleep(150)
                                    } catch (e2: InterruptedException) {
                                        e2.printStackTrace()
                                    }
                                }
                            }
                        } catch (unused2: java.lang.Exception) {
                        }
                    }
                    null
                }
                else -> {
                    val packageInfo =
                        homeFragment.mPackageManager!!.getInstalledApplications(PackageManager.GET_META_DATA)
                    for (next2 in packageInfo) {
                        if (homeFragment.mPackageManager == null) {
                            return null
                        }
                        try {
                            if (!next2.packageName.contains(activity.packageName)
                                && Utils.isUserApp(next2)
                                && !Utils.checkLockedItem(activity, next2.packageName)
                            ) {
                                val taskInfo3 = TaskInfo(activity, next2)
                                homeFragment.mActivityManager!!.killBackgroundProcesses(taskInfo3.appinfo.packageName)
                                val applicationIcon3 =
                                    activity.packageManager.getApplicationIcon(taskInfo3.appinfo.packageName)
                                publishProgress(*arrayOf(applicationIcon3))
                                try {
                                    Thread.sleep(150)
                                } catch (e3: InterruptedException) {
                                    e3.printStackTrace()
                                }
                            }
                        } catch (e4: PackageManager.NameNotFoundException) {
                            e4.printStackTrace()
                        }
                    }
                    null
                }
            }
        }

        override fun onProgressUpdate(vararg drawableArr: Drawable) {
            val nextInt = Random().nextInt(homeFragment.arrGravity1.size - 1 + 1) + 0
            val sb = StringBuilder()
            sb.append("RANDOM: ")
            sb.append(nextInt)
            sb.append(" curIndex: ")
            sb.append(homeFragment.curIndex)
            val dimension = activity.resources.getDimension(R.dimen.icon_size).toInt()
            val layoutParams = FrameLayout.LayoutParams(-2, -2)
            layoutParams.height = dimension
            layoutParams.width = dimension
            layoutParams.gravity = homeFragment.arrGravitys[homeFragment.curIndex][nextInt]
            val animation = when (homeFragment.curIndex) {
                0 -> {
                    AnimationUtils.loadAnimation(activity, R.anim.anim_item_boost_1)
                }
                1 -> {
                    AnimationUtils.loadAnimation(activity, R.anim.anim_item_boost_2)
                }
                2 -> {
                    AnimationUtils.loadAnimation(activity, R.anim.anim_item_boost_3)
                }
                3 -> {
                    AnimationUtils.loadAnimation(activity, R.anim.anim_item_boost_4)
                }
                else -> {
                    AnimationUtils.loadAnimation(activity, R.anim.anim_item_boost_0)
                }
            }
            val imageView = ImageView(activity)

            imageView.setImageDrawable(drawableArr[0])
            imageView.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation) {
                    Log.e("dd", "")
                }

                override fun onAnimationStart(animation: Animation) {
                    Log.e("dd", "")
                }

                override fun onAnimationEnd(animation: Animation) {
                    imageView.visibility = View.GONE
                }
            })
            super.onProgressUpdate(*drawableArr)
        }

        public override fun onPostExecute(voidR: Void?) {
            super.onPostExecute(voidR)
        }

        init {
            homeFragment.mPackageManager = activity.packageManager
            homeFragment.mActivityManager =
                activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }
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
        didOptimize = false
        // Update UI
        updateOptimizeButton(true)

        val arrOptimizationDescProcess = arrayListOf<String>().apply {
            ctx?.appSettingsModel?.run {
                if (isCharging) {
                    add(getString(R.string.clean_apps))
                }
                if (isTurnOffBluetooth) {
                    add(getString(R.string.turn_off_bluetooth))
                }
                if (!PermissionUtil.isApi29orHigher() && isTurnOffWifi) {
                    add(getString(R.string.turn_off_wifi))
                }
                if (isTurnOffAutoSync) {
                    add(getString(R.string.turn_off_auto_sync))
                }
                if (isTurnOffScreenRotation) {
                    add(getString(R.string.turn_off_screen_rotation))
                }

                if (isClearRam) {
                    add(getString(R.string.clear_ram))
                }

                if (isCharging && isReduceScreenTimeOut) {
                    add(getString(R.string.reduce_screen_timeout))
                }
            }
        }
        presenter.showOptimizationDescProcess(arrOptimizationDescProcess.size) {
            lblOptimizationDescProcess.text = arrOptimizationDescProcess[it]
        }

        // Save optimization times
        ctx?.appSettingsModel?.run {
            optimizedTimes += 1
            CommonUtil.saveAppSettingsModel(ctx, this)

            // Load interstitial ad before
            if (!isShowRateDialog()) {
                AdsManager.loadInterstitialOptimizationResult(ctx)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BatteryStatusReceiver.unregister(ctx, batteryInfoReceiver)
    }

    override fun onOptimizationSuccess() {
        // Show success screen
        showOptimizationResultScreen(true)
        animViewOptimization.visible()
        // Update flags
        didOptimize = true
        isJustOpenedApp = false

        // Update UI
        updateOptimizeButton()
        lblOptimizationDescProcess.text = ""
    }

    override fun onAppSettingsChanged(model: AppSettingsModel) {
        if (ctx?.shouldShowAds() != true) {
            // Update UI
//            frlBannerAd.gone()
        }
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
        if (!didOptimize || forceOptimize) {
            if (currentPercentage != 100f && ctx?.appSettingsModel?.batteryPercentage != 100f) {
                presenter.optimise(animViewOptimization, isCharging)
                updateBrightness()
            }
        } else {
            showOptimizationResultScreen(false)
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
        Log.d("HaiHT", "refreshOptimizationUIState")
        if (didOptimize) {
            if (shouldOptimize()) {
                didOptimize = false
                updateOptimizeButton()
            }
        } else {
            if (!shouldOptimize()) {
                didOptimize = true
                updateOptimizeButton()
            }
        }
    }

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
            val plugged = getPlugged(context!!)
            val usbCharge = plugged == BatteryManager.BATTERY_PLUGGED_USB
            val time: Int = if (usbCharge) {
                BatteryPref.initilaze(context)!!
                    .getTimeChargingUsb(context!!, getBatteryLevel(context!!))
            } else {
                BatteryPref.initilaze(context)!!
                    .getTimeChargingAc(context!!, getBatteryLevel(context!!))
            }
            txtInfo.text = getString(R.string.time_charging_left)
            txtHours.text = (time / 60).toString()
            txtMin.text = (time % 60).toString()
        } else {
            val time = BatteryPref.initilaze(context)!!
                .getTimeRemainning(context!!, getBatteryLevel(context!!))
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

    private fun showOptimizationResultScreen(checkShowingRateDialog: Boolean) {
        bundleOf(com.sh.entertainment.fastcharge.common.Constants.KEY_CHECK_SHOWING_RATE_DIALOG to checkShowingRateDialog).run {
            openActivity(OptimizationResultActivity::class.java, this)
        }
    }

    private fun updateOptimizeButton(isOptimizing: Boolean = false) {
        if (didOptimize) {
            btnOptimize.apply {
                isEnabled = true
                text = getString(R.string.optimized)
                setBackgroundResource(R.drawable.btn_green)
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tick_circle, 0, 0, 0)
            }
        } else {
            btnOptimize.apply {
                text = if (isOptimizing) {
                    isEnabled = false
                    setBackgroundResource(R.drawable.btn_blue_black)
                    getString(R.string.optimizing)
                } else {
                    isEnabled = true
                    setBackgroundResource(R.drawable.btn_yellow)
                    getString(R.string.optimize)
                }
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