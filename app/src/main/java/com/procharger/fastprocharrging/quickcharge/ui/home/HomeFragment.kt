package com.procharger.fastprocharrging.quickcharge.ui.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdRequest
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.MyApplication
import com.procharger.fastprocharrging.quickcharge.common.extension.*
import com.procharger.fastprocharrging.quickcharge.common.util.*
import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import com.procharger.fastprocharrging.quickcharge.data.model.BatteryModel
import com.procharger.fastprocharrging.quickcharge.data.model.TaskInfo
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseFragment
import com.procharger.fastprocharrging.quickcharge.ui.boresult.OptimizationResultActivity
import com.procharger.fastprocharrging.quickcharge.widget.ads.LayoutNativeAd
import com.skyfishjy.library.RippleBackground
import java.util.*

class HomeFragment : BaseFragment<HomeView, HomePresenterImp>(), HomeView {

    private lateinit var scrollView: NestedScrollView
    private lateinit var imgHelp: ImageView
    private lateinit var animViewPercentage: LottieAnimationView
    private lateinit var animViewOptimization: LottieAnimationView
    private lateinit var btnOptimize: TextView
    private lateinit var lblOptimizationDescProcess: TextView
    private lateinit var lblWarnUsbCharging: TextView
    private lateinit var lblPercentage: TextView
    private lateinit var frlBannerAd: FrameLayout
    private lateinit var nativeAdView: LayoutNativeAd

    private lateinit var txtInfo: TextView
    private lateinit var txtMin: TextView
    private lateinit var txtHours: TextView
    private lateinit var viewTimeLeft: LinearLayout
    private lateinit var lytListItem: LinearLayout

    var chargeBoostContainers: ArrayList<FrameLayout> = ArrayList()
    private lateinit var rocketImage: ImageView
    private lateinit var rocketImage2: ImageView
    private lateinit var rocketImageOut: ImageView
    private lateinit var animationOptimization: RippleBackground


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
    private var previousBatteryPercentage = -1
    private var isCharging = false
    private var previousChargingState = false
    private var shouldUpdatePercentageFrame = false
    private var didOptimize = false
    private var isJustOpenedApp = true // Always allow user to optimize when open app
    private var didLoadBannerAd = false
    private var percentageCurrent = 0

    private val animPercentageFadeOut by lazy {
        AnimationUtils.loadAnimation(ctx, R.anim.fade_out).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    animViewPercentage.playAnimation()
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            })
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if #forceOptimize is true then call #startOptimizing() method immediately
        val forceOptimize = arguments?.getBoolean(ARG_FORCE_OPTIMIZE) ?: false
        if (forceOptimize) {
            startOptimizing(true)
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
            hideHelpIcon()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        // Unregister battery status receiver
        BatteryStatusReceiver.unregister(ctx, batteryStatusReceiver)

        // Remove battery animator listener
        removeBatteryAnimatorListener()

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
            imgHelp = findViewById(R.id.img_help)
            animViewPercentage = findViewById(R.id.anim_percentage)
            animViewOptimization = findViewById(R.id.anim_optimization)
            btnOptimize = findViewById(R.id.btn_optimize)
            lblOptimizationDescProcess = findViewById(R.id.lbl_optimization_desc_process)
            lblWarnUsbCharging = findViewById(R.id.lbl_warn_usb_charging)
            lblPercentage = findViewById(R.id.lbl_percentage)
//            frlBannerAd = findViewById(R.id.frl_banner_ad)
            nativeAdView = findViewById(R.id.nativeAdView)

            lytListItem = findViewById(R.id.lytListItem)
            viewTimeLeft = findViewById(R.id.view_time_left)
            txtInfo = findViewById(R.id.txtInfo)
            txtMin = findViewById(R.id.tvMin)
            txtHours = findViewById(R.id.tvHour)

            chargeBoostContainers.add(findViewById<View>(R.id.fm_scan_container_1) as FrameLayout)
            chargeBoostContainers.add(findViewById<View>(R.id.fm_scan_container_2) as FrameLayout)
            chargeBoostContainers.add(findViewById<View>(R.id.fm_scan_container_3) as FrameLayout)
            chargeBoostContainers.add(findViewById<View>(R.id.fm_scan_container_4) as FrameLayout)
            animationOptimization = findViewById(R.id.charge_boost_ripple_background)
            rocketImage = findViewById<View>(R.id.ivScan) as ImageView
            rocketImage2 = findViewById<View>(R.id.ivScan2) as ImageView
            rocketImageOut = findViewById<View>(R.id.ivDoneHoloCirular) as ImageView
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

        /*if (PermissionUtil.isApi23orHigher()) {
            ctx?.run {
                if (!canDrawOverlay()) {
                    if (appSettingsModel.appOpenedTimes >= 2) {
                      //  imgHelp.visible()
                        imgHelp.setOnSafeClickListener {
                            showDrawOverlayPermissionDescDialog(self, RC_DRAW_OVERLAY)
                        }
                    }
                }
            }
        }*/

        val adRequest = AdRequest.Builder()
            .build()
        try {
            if (MyApplication.remoteConfigModel.is_native_home) {
                nativeAdView.showAd(adRequest, MyApplication.KEY_NATIVE)
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
                                val applicationIcon3 = activity.packageManager.getApplicationIcon(taskInfo3.appinfo.packageName)
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
            homeFragment.chargeBoostContainers[homeFragment.curIndex]
                .addView(imageView, layoutParams)

            homeFragment.curIndex = homeFragment.curIndex + 1
            if (homeFragment.curIndex >= homeFragment.chargeBoostContainers.size) {
                homeFragment.curIndex = 0
            }
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

    //----------------------------------------------------------------------------------------------

    private fun initRippleBackground() {
        animationOptimization.startRippleAnimation()
        val animatorSet = AnimatorSet()
        animatorSet.duration = 400
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        val arrayList: ArrayList<Animator> = ArrayList()
        arrayList.add(
            ObjectAnimator.ofFloat(
                rocketImageOut,
                "ScaleX",
                0.0f, 1.2f, 1.0f
            )
        )
        arrayList.add(
            ObjectAnimator.ofFloat(
                rocketImageOut,
                "ScaleY",
                0.0f, 1.2f, 1.0f
            )
        )
        animatorSet.playTogether(arrayList)
        animatorSet.start()
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
                if(isCharging) {
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

    override fun onOptimizationSuccess() {
        // Show success screen
        showOptimizationResultScreen(true)
        animViewOptimization.visible()
        animationOptimization.gone()
        // Update flags
        didOptimize = true
        isJustOpenedApp = false

        // Update UI
        updateOptimizeButton()
        lblOptimizationDescProcess.text = ""

        updateBatteryPercentageAnimator(previousBatteryPercentage, true, 100)
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
                presenter.optimiseV2( animationOptimization,lytListItem,rocketImage)
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
                updateBatteryPercentageAnimator(previousBatteryPercentage, true, 100)
                updateOptimizeButton()
            }
        } else {
            if (!shouldOptimize()) {
                didOptimize = true
                updateBatteryPercentageAnimator(previousBatteryPercentage, true, 100)
                updateOptimizeButton()
            }
        }
    }

    fun hideHelpIcon() {
        ctx?.run {
            imgHelp.gone()
        }
    }

    private fun fillBatteryInfo(model: BatteryModel) {
        with(model) {
            // Battery percentage
            lblPercentage.text =
                String.format(getString(R.string._percentage), NumberUtil.formatNumber(percentage))

            // Show/hide usb charging warning
            /* if (isPluggedUsb) {
                 lblWarnUsbCharging.visible()
             } else {
                 lblWarnUsbCharging.gone()
             }*/

            // Percentage effect
            percentage?.toInt()?.run {
                if (percentageCurrent != this) {
                    updateBatteryPercentageAnimator(percentage = this)
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
        bundleOf(com.procharger.fastprocharrging.quickcharge.common.Constants.KEY_CHECK_SHOWING_RATE_DIALOG to checkShowingRateDialog).run {
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

    private fun updateBatteryPercentageAnimator(
        percentage: Int,
        forceUpdate: Boolean = false,
        startFrame: Int = 0
    ) {
        Log.d("HaiHT", "updateBatteryPercentageAnimator")

        var animRes = 0
        var shouldLoop = true
        var shouldUpdateAnim = false
        var maxFrameVertical = 0
        when {
            percentage < 15 -> {
                if (previousBatteryPercentage !in 0 until 15 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_10
                    } else {
                        R.raw.lt_percentage_optimizing_effect_10
                    }
                    shouldLoop = true
                    maxFrameVertical = 20
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 25 -> {
                if (previousBatteryPercentage !in 15 until 25 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_20
                    } else {
                        R.raw.lt_percentage_optimizing_effect_20
                    }
                    shouldLoop = true
                    maxFrameVertical = 30
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 35 -> {
                if (previousBatteryPercentage !in 25 until 35 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_30
                    } else {
                        R.raw.lt_percentage_optimizing_effect_30
                    }
                    shouldLoop = true
                    maxFrameVertical = 40
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 45 -> {
                if (previousBatteryPercentage !in 35 until 45 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_40
                    } else {
                        R.raw.lt_percentage_optimizing_effect_40
                    }
                    shouldLoop = true
                    maxFrameVertical = 50
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 55 -> {
                if (previousBatteryPercentage !in 45 until 55 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_50
                    } else {
                        R.raw.lt_percentage_optimizing_effect_50
                    }
                    shouldLoop = true
                    maxFrameVertical = 60
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 65 -> {
                if (previousBatteryPercentage !in 55 until 65 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_60
                    } else {
                        R.raw.lt_percentage_optimizing_effect_60
                    }
                    shouldLoop = true
                    maxFrameVertical = 60
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 75 -> {
                if (previousBatteryPercentage !in 65 until 75 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_70
                    } else {
                        R.raw.lt_percentage_optimizing_effect_70
                    }
                    shouldLoop = true
                    maxFrameVertical = 60
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 85 -> {
                if (previousBatteryPercentage !in 75 until 85 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_80
                    } else {
                        R.raw.lt_percentage_optimizing_effect_80
                    }
                    shouldLoop = true
                    maxFrameVertical = 70
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 95 -> {
                if (previousBatteryPercentage !in 85 until 95 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_90
                    } else {
                        R.raw.lt_percentage_optimizing_effect_90
                    }
                    shouldLoop = true
                    maxFrameVertical = 70
                    shouldUpdatePercentageFrame = true
                }
            }
            percentage < 100 -> {
                if (previousBatteryPercentage !in 95 until 100 || forceUpdate) {
                    shouldUpdateAnim = true
                    animRes = if (didOptimize) {
                        R.raw.lt_percentage_optimized_effect_95
                    } else {
                        R.raw.lt_percentage_optimizing_effect_95
                    }
                    shouldLoop = true
                    maxFrameVertical = 70
                    shouldUpdatePercentageFrame = true
                }
            }
            else -> {
                if (previousBatteryPercentage != 100 || forceUpdate) {
                    shouldUpdateAnim = true
//                    animRes = if (didOptimize) {
//                        R.raw.lt_percentage_optimized_effect_100
//                    } else {
//                        R.raw.lt_percentage_optimizing_effect_100
//                    }
                    animRes = R.raw.lt_percentage_optimized_effect_100
                    shouldLoop = false
                }
            }
        }

        // Update anim resource
        if (shouldUpdateAnim) {
            removeBatteryAnimatorListener()
            animViewPercentage.apply {
                setAnimation(animRes)
                setMinFrame(startFrame)
                if (isCharging && percentage < 100 && didOptimize) {
                    setMinAndMaxFrame(0, maxFrameVertical)
                }
                addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {
                        if (!didLoadBannerAd) {
                            presenter.delayBeforeDoing(2100) {
                                loadBannerAd()
                            }
                        }
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        if (shouldLoop) {
                            if (isCharging && percentage < 100 && didOptimize) {
                                setMinMaxFrames(0, maxFrameVertical)
                                clearAnimation()
                                startAnimation(animPercentageFadeOut)
                            } else {
                                setMinMaxFrames(120, 200)
                                playAnimation()
                            }
                        }
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                    }
                })
                playAnimation()
            }

            // Keep new battery percentage
            previousBatteryPercentage = percentage
        }
    }

    private fun removeBatteryAnimatorListener() {
        animViewPercentage.removeAllAnimatorListeners()
    }

    private fun shouldOptimize(): Boolean {
        return ctx?.let { ctx ->
            (ctx.appSettingsModel.isTurnOffBluetooth && ctx.isBluetoothEnabled) ||
                    (ctx.appSettingsModel.isTurnOffAutoSync && ctx.isAutoSyncEnabled) ||
                    (ctx.appSettingsModel.isTurnOffScreenRotation && ctx.isAutoRotationEnabled) ||
                    isJustOpenedApp
        } ?: false
    }

    private fun setMinMaxFrames(min: Int, max: Int) {
        if (shouldUpdatePercentageFrame || previousChargingState != isCharging) {
            animViewPercentage.setMinAndMaxFrame(min, max)
            shouldUpdatePercentageFrame = false
            previousChargingState = isCharging
        }
    }

    private fun loadBannerAd() {
//        ctx?.run {
//            adsConfigModel.run {
//                // Show banner ad
//                if (isBannerHomeEnabled) {
//                    frlBannerAd.visible()
//                    AdsManager.showBannerAd(
//                        parentActivity,
//                        frlBannerAd,
//                        adIdBannerHome
//                    ) {
//                        didLoadBannerAd = true
//                    }
//                }
//            }
//        }
    }
}