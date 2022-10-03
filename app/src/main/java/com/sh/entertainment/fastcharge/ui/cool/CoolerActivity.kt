package com.sh.entertainment.fastcharge.ui.cool

import android.animation.Animator
import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.common.util.Utils
import com.sh.entertainment.fastcharge.data.model.TaskInfo
import com.sh.entertainment.fastcharge.databinding.ActivityCoolerBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*


class CoolerActivity : BaseActivityBinding<ActivityCoolerBinding>() {

    private var arrGravity1: IntArray = intArrayOf(49, 19, 83)
    private var arrGravity2: IntArray = intArrayOf(51, 49, 21)
    private var arrGravity3: IntArray = intArrayOf(53, 21, 81)
    private var arrGravity4: IntArray = intArrayOf(85, 81, 19)

    private var arrGravitys: Array<IntArray> =
        arrayOf(arrGravity1, arrGravity2, arrGravity3, arrGravity4)
    var curIndex = 0

    private var mPackageManager: PackageManager? = null
    private var mActivityManager: ActivityManager? = null

    private var chargeBoostContainers: ArrayList<FrameLayout> = ArrayList()

    var didCooler = false

    override val layoutId = R.layout.activity_cooler

    override fun initializeView() {
        chargeBoostContainers.add(dataBinding.fmScanContainer1)
        chargeBoostContainers.add(dataBinding.fmScanContainer2)
        chargeBoostContainers.add(dataBinding.fmScanContainer3)
        chargeBoostContainers.add(dataBinding.fmScanContainer4)
    }

    override fun initializeData() {
        dataBinding.animSnow.gone()
        dataBinding.animDone.gone()

        handleLoadInter()
        setupAnim()
        AdsManager.showNativeAd(this, dataBinding.nativeAdView, AdsManager.NATIVE_AD_KEY)

        handleOptimizeCooler()
        dataBinding.animSnow.visible()
        dataBinding.animSnow.apply {
            removeAllAnimatorListeners()
            cancelAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    dataBinding.animSnow.gone()
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            playAnimation()
        }
    }

    override fun onClick() {
//        dataBinding.btnOptimize.setOnClickListener {
//            if (didCooler) {
//                finish()
//                return@setOnClickListener
//            }
//            handleOptimizeCooler()
//            dataBinding.animSnow.visible()
//            dataBinding.animSnow.apply {
//                removeAllAnimatorListeners()
//                cancelAnimation()
//                addAnimatorListener(object : Animator.AnimatorListener {
//                    override fun onAnimationStart(p0: Animator?) {
//                    }
//
//                    override fun onAnimationEnd(p0: Animator?) {
//                        dataBinding.animSnow.gone()
//                    }
//
//                    override fun onAnimationCancel(p0: Animator?) {
//                    }
//
//                    override fun onAnimationRepeat(p0: Animator?) {
//                    }
//                })
//                playAnimation()
//            }
//        }
    }

    private fun handleOptimizeCooler() {
        dataBinding.lytListItem.visible()
        val animationView = AnimationUtils.loadAnimation(this, R.anim.rote_charge_anim)
        dataBinding.ivScan.startAnimation(animationView)
        dataBinding.ivScan2.startAnimation(animationView)
        animationView.apply {
            start()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    optimizeCoolerCore()
                    killAllAppOnBackground()
                }

                override fun onAnimationEnd(p0: Animation?) {
                    didCooler = true
                    dataBinding.txtMessage.text = getString(R.string.optimized)
//                    dataBinding.btnOptimize.gone()
//                    dataBinding.btnOptimize.apply {
//                        background =
//                            ContextCompat.getDrawable(this@CoolerActivity, R.drawable.btn_green)
//                        text = getString(R.string.cooler_optimized)
//                    }
                    endTaskAndStartAnimDone()
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            })
        }
    }

    private fun endTaskAndStartAnimDone() {
        dataBinding.chargeBoostRippleBackground.gone()
        dataBinding.animDone.visible()
        dataBinding.animDone.apply {
            removeAllAnimatorListeners()
            cancelAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    showInter()
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            playAnimation()
        }
    }

    private fun optimizeCoolerCore() {
        if (canWriteSettings()) {
            toggleAutoRotation(0)
            toggleAutoSync(false)
        }
    }

    private fun killAllAppOnBackground() {
        val packages: List<ApplicationInfo>
        val pm: PackageManager = packageManager
        packages = pm.getInstalledApplications(0)

        val myPackage = applicationContext.packageName
        for (packageInfo in packages) {
            if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) continue
            if (packageInfo.packageName == myPackage) continue
            if (packageInfo.packageName != ctx.packageName) {
                (getSystemService(ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(
                    packageInfo.packageName
                )
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private suspend fun getAppIcon() = withContext(Dispatchers.IO) {
        mPackageManager = this@CoolerActivity.packageManager
        mActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (mActivityManager == null) {
            val listResults = ArrayList<Drawable>()
            listResults
        } else {
            val runningAppProcesses = mActivityManager!!.runningAppProcesses
            when {
                Build.VERSION.SDK_INT <= 21 -> {
                    val listResults = ArrayList<Drawable>()
                    for (runningAppProcessInfo in runningAppProcesses) {
                        try {
                            if (mPackageManager != null) {
                                val str = runningAppProcessInfo.processName
                                val applicationInfo = mPackageManager!!.getApplicationInfo(str, 0)
                                if (!str.contains(packageName)
                                    && Utils.isUserApp(applicationInfo) && !Utils.checkLockedItem(
                                        this@CoolerActivity,
                                        str
                                    )
                                ) {
                                    val taskInfo = TaskInfo(this@CoolerActivity, applicationInfo)
                                    if (taskInfo.appinfo.packageName != ctx.packageName) {
                                        mActivityManager!!.killBackgroundProcesses(taskInfo.appinfo.packageName)
                                    }
                                    val applicationIcon =
                                        this@CoolerActivity.packageManager.getApplicationIcon(
                                            taskInfo.appinfo.packageName ?: ""
                                        )
                                    listResults.add(applicationIcon)
                                }
                            }

                        } catch (unused: Exception) {
                            Log.e("fff", "")
                        }
                    }
                    listResults
                }
                Build.VERSION.SDK_INT < 26 -> {
                    val listResults = ArrayList<Drawable>()
                    for (next in mActivityManager!!.getRunningServices(Int.MAX_VALUE)) {
                        try {
                            if (mPackageManager != null) {
                                val packageInfo = mPackageManager!!.getPackageInfo(
                                    next.service.packageName,
                                    PackageManager.GET_ACTIVITIES
                                )

                                if (packageInfo != null) {
                                    val applicationInfo2 = mPackageManager!!.getApplicationInfo(
                                        packageInfo.packageName,
                                        0
                                    )
                                    if (!packageInfo.packageName.contains(this@CoolerActivity.packageName)
                                        && Utils.isUserApp(applicationInfo2)
                                        && !Utils.checkLockedItem(
                                            this@CoolerActivity,
                                            packageInfo.packageName
                                        )
                                    ) {
                                        val taskInfo2 =
                                            TaskInfo(this@CoolerActivity, applicationInfo2)
                                        if (taskInfo2.appinfo.packageName != ctx.packageName) {
                                            mActivityManager!!.killBackgroundProcesses(taskInfo2.appinfo.packageName)
                                        }
                                        val applicationIcon2 =
                                            this@CoolerActivity.packageManager.getApplicationIcon(
                                                taskInfo2.appinfo.packageName ?: ""
                                            )
                                        listResults.add(applicationIcon2)
                                    }
                                }
                            }

                        } catch (unused2: Exception) {
                        }
                    }
                    listResults
                }
                else -> {
                    val listResults = ArrayList<Drawable>()
                    val packageInfo =
                        mPackageManager!!.getInstalledApplications(PackageManager.GET_META_DATA)
                    for (next2 in packageInfo) {
                        if (mPackageManager != null) {
                            try {
                                if (!next2.packageName.contains(this@CoolerActivity.packageName)
                                    && Utils.isUserApp(next2)
                                    && !Utils.checkLockedItem(
                                        this@CoolerActivity,
                                        next2.packageName
                                    )
                                ) {
                                    val taskInfo3 = TaskInfo(this@CoolerActivity, next2)
                                    if (taskInfo3.appinfo.packageName != ctx.packageName) {
                                        mActivityManager!!.killBackgroundProcesses(taskInfo3.appinfo.packageName)
                                    }
                                    val applicationIcon3 =
                                        this@CoolerActivity.packageManager.getApplicationIcon(
                                            taskInfo3.appinfo.packageName
                                        )
                                    listResults.add(applicationIcon3)
                                }
                            } catch (e4: PackageManager.NameNotFoundException) {
                                e4.printStackTrace()
                            }
                        }
                    }
                    listResults
                }
            }
        }
    }

    private fun setupAnim() {
        val list = runBlocking { getAppIcon() }
        list.forEach {
            setupAnimationDetail(it)
        }
    }

    private fun setupAnimationDetail(drawableArr: Drawable) {
        val nextInt = Random().nextInt(arrGravity1.size - 1 + 1) + 0
        val sb = StringBuilder()
        sb.append("RANDOM: ")
        sb.append(nextInt)
        sb.append(" curIndex: ")
        sb.append(curIndex)
        val dimension = resources.getDimension(R.dimen.icon_size).toInt()
        val layoutParams = FrameLayout.LayoutParams(-2, -2)
        layoutParams.height = dimension
        layoutParams.width = dimension
        layoutParams.gravity = arrGravitys[curIndex][nextInt]
        val animation = when (curIndex) {
            0 -> {
                AnimationUtils.loadAnimation(this, R.anim.anim_item_boost_1)
            }
            1 -> {
                AnimationUtils.loadAnimation(this, R.anim.anim_item_boost_2)
            }
            2 -> {
                AnimationUtils.loadAnimation(this, R.anim.anim_item_boost_3)
            }
            3 -> {
                AnimationUtils.loadAnimation(this, R.anim.anim_item_boost_4)
            }
            else -> {
                AnimationUtils.loadAnimation(this, R.anim.anim_item_boost_0)
            }
        }
        val imageView = ImageView(this)
        chargeBoostContainers[curIndex]
            .addView(imageView, layoutParams)

        curIndex += 1
        if (curIndex >= chargeBoostContainers.size) {
            curIndex = 0
        }
        imageView.setImageDrawable(drawableArr)
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
    }

    private fun showInter() {
        if (MyApplication.interstitialAd == null) {
            finish()
            return
        }
        if ((System.currentTimeMillis() - MyApplication.timeShowIntel) < MyApplication.remoteConfigModel.timeShowInter * 1000) {
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
        InterstitialAd.load(this, MyApplication.remoteConfigModel.keyIntel, adRequest,
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