package com.procharger.fastprocharrging.quickcharge.ui.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.MyApplication
import com.procharger.fastprocharrging.quickcharge.common.extension.*
import com.procharger.fastprocharrging.quickcharge.common.util.CommonUtil
import com.procharger.fastprocharrging.quickcharge.common.util.PermissionUtil
import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseActivity
import com.procharger.fastprocharrging.quickcharge.ui.home.HomeFragment
import com.procharger.fastprocharrging.quickcharge.ui.home.PowerConnectionService
import com.procharger.fastprocharrging.quickcharge.ui.info.InfoActivity
import com.procharger.fastprocharrging.quickcharge.ui.settings.SettingsActivity
import com.procharger.fastprocharrging.quickcharge.widget.ads.LayoutNativeAd
import android.content.DialogInterface
import android.view.*
import com.procharger.fastprocharrging.quickcharge.ui.chargehistory.ChargeHistoryActivity


class MainActivity : BaseActivity<MainView, MainPresenterImp>(), MainView,
    NavigationView.OnNavigationItemSelectedListener {

    private val toolbarMain by lazy { findViewById<Toolbar>(R.id.toolbar_main) }
    private val drawerMain by lazy { findViewById<DrawerLayout>(R.id.drawer_main) }
    private val navMain by lazy { findViewById<NavigationView>(R.id.nav_main) }
    private val appBarLayout by lazy { findViewById<AppBarLayout>(R.id.appbar) }
    private val imageClose by lazy { findViewById<ImageView>(R.id.btnback) }
    private val lytRateApp by lazy { findViewById<LinearLayout>(R.id.lytRateApp) }
    private val lytInfo by lazy { findViewById<LinearLayout>(R.id.lytInfo) }
    private val shareApp by lazy { findViewById<LinearLayout>(R.id.lytShareApp) }
    private val policy by lazy { findViewById<LinearLayout>(R.id.lytPolicy) }
    private val updateApp by lazy { findViewById<LinearLayout>(R.id.lytRefresh) }
    private val lytChargeHistory by lazy { findViewById<LinearLayout>(R.id.lytChargeHistory) }


    private var forceOptimize = false
    private val frgHome by lazy { HomeFragment.newInstance(forceOptimize) }
    private var menuItemAd: MenuItem? = null
    private var navItemAd: MenuItem? = null

    private var timeClickBackPress = 0L

    companion object {
        private const val RC_OPEN_SETTINGS_PAGE = 256
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Save app opened times
        appSettingsModel.run {
            if (appOpenedTimes < 2) {
                appOpenedTimes++
                CommonUtil.saveAppSettingsModel(ctx, this)
            }
        }

        MyApplication.instance.loadNativeExit()
        // Add Home fragment
        parseIntent(intent)
        addFragment(R.id.frl_main, frgHome)

        // Reset #forceOptimize var
        forceOptimize = false
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        parseIntent(intent)

        if (forceOptimize) {
            frgHome.startOptimizing()
            forceOptimize = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_OPEN_SETTINGS_PAGE && resultCode == RESULT_OK) {
            frgHome.hideHelpIcon()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItemSettings = menu?.findItem(R.id.menu_settings)
        val settingsActionView = menuItemSettings?.actionView as? FrameLayout
        settingsActionView?.setOnSafeClickListener {
            onOptionsItemSelected(menuItemSettings)
        }

        if (shouldShowAdsRemovalFeature()) {
            val menuItemRemoveAds = menu?.findItem(R.id.menu_remove_ads)
            val removeAdsActionView = menuItemRemoveAds?.actionView as? FrameLayout
            removeAdsActionView?.setOnSafeClickListener {
                onOptionsItemSelected(menuItemRemoveAds)
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (shouldShowAdsRemovalFeature()) {
            menuItemAd = menu?.findItem(R.id.menu_remove_ads)?.apply {
                isVisible = true
                actionView?.findViewById<LottieAnimationView>(R.id.lt_remove_ads)?.playAnimation()
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val vibration = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (PermissionUtil.isApi26orHigher()) {
                    vibration.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    );
                } else {
                    //deprecated in API 26
                    vibration.vibrate(100)
                }
                openActivityForResult(SettingsActivity::class.java, RC_OPEN_SETTINGS_PAGE)
                return true
            }
            R.id.nav_history -> {
                //removeAds()
                val vibration = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (PermissionUtil.isApi26orHigher()) {
                    vibration.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    //deprecated in API 26
                    vibration.vibrate(100)
                }
                openActivity(ChargeHistoryActivity::class.java)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isXiaomiDevice && hasFocus) {
            frgHome.refreshOptimizationUIState()
        }
    }

    override fun onBackPressed() {
        if (drawerMain.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            if (MyApplication.remoteConfigModel.is_native_exit_app && MyApplication.nativeAdExit != null) {
                timeClickBackPress = System.currentTimeMillis()
                showDialogExit()
            } else {
                finishApp()
            }
        }
    }

    private fun finishApp() {
        finishAffinity()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun showDialogExit() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.fragment_bottom_sheet_ads)
        val nativeAd = dialog.findViewById<LayoutNativeAd>(R.id.nativeAdView)
        val btnExit = dialog.findViewById<TextView>(R.id.btnCloseApp)

        val adView =
            FrameLayout.inflate(ctx, R.layout.layout_native_bottom, null) as UnifiedNativeAdView
        nativeAd?.apply {
            populateUnifiedNativeAdView(MyApplication.nativeAdExit!!, adView)
            removeAllViews()
            addView(adView)
        }
        dialog.setOnKeyListener { _, keyCode, _ -> // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (System.currentTimeMillis() - timeClickBackPress < 5000) {
                    finishApp()
                } else {
                    dialog.dismiss()
                }
            }
            true
        }
        btnExit?.setOnClickListener {
            finishApp()
        }
        if(!dialog.isShowing) {
            dialog.show()
        }
    }

    override fun initView(): MainView {
        return this
    }

    override fun initPresenter(): MainPresenterImp {
        return MainPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_main
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()
        applyToolbar(toolbarMain)
        showTitle(R.string.fast_charger, toolbar = toolbarMain)
        for (child in toolbarMain.children) {
            if (child is TextView) {
                child.apply {
                    ellipsize = TextUtils.TruncateAt.MARQUEE
                    isSelected = true
                    marqueeRepeatLimit = -1
                    isHorizontalFadingEdgeEnabled = true
                }
            }
        }

        // Init navigation
        val toggle = ActionBarDrawerToggle(
            self,
            drawerMain,
            toolbarMain,
            R.string.app_name,
            R.string.app_name
        )
        toggle.isDrawerIndicatorEnabled = false
        drawerMain.addDrawerListener(toggle)
        toggle.setHomeAsUpIndicator(R.drawable.ic_drawer)
        toggle.setToolbarNavigationClickListener {
            drawerMain.openDrawer(GravityCompat.START)
        }
        toggle.syncState()
        navMain.setNavigationItemSelectedListener(this)
        navMain.itemIconTintList = null
        /*if (shouldShowAdsRemovalFeature()) {
            navItemAd = navMain.menu.findItem(R.id.nav_remove_ads)
            navItemAd?.isVisible = true
        }*/
//        navMain.menu.findItem(R.id.nav_info);
//        navMain.getHeaderView(0).setOnSafeClickListener {
//            closeDrawer()
//        }
        imageClose.setOnClickListener {
            closeDrawer()
        }

        lytRateApp.setOnClickListener {
            closeDrawer()
            presenter.delayBeforeDoing(250) {
                showRateDialog()
            }
        }
        lytInfo.setOnClickListener {
            closeDrawer()
            openActivity(InfoActivity::class.java)
        }
        shareApp.setOnClickListener {
            closeDrawer()
            CommonUtil.shareText(
                ctx,
                com.procharger.fastprocharrging.quickcharge.common.Constants.LINK_APP_ON_STORE
            )
        }
        policy.setOnClickListener {
            closeDrawer()
            CommonUtil.openBrowser(
                ctx,
                com.procharger.fastprocharrging.quickcharge.common.Constants.LINK_POLICY
            )
        }
        updateApp.setOnClickListener {
            closeDrawer()
            CommonUtil.openAppInPlayStore(ctx)
        }

        lytChargeHistory.setOnClickListener {
            closeDrawer()
            openActivity(ChargeHistoryActivity::class.java)
        }

        MyApplication.showRateDialog.observe(this) {
            if (it && !appSettingsModel.dontShowRateDialogAgain) {
                showRateAppDialog {}
            }

            MyApplication.showRateDialog.postValue(false)
        }
        // Disable scroll content when swipe on appbar if ads is disabled
        if (!shouldShowAds() || !adsConfigModel.isBannerHomeEnabled) {
            presenter.delayBeforeDoing(200) {
                val params = appBarLayout.layoutParams as? CoordinatorLayout.LayoutParams
                val behavior = params?.behavior as? AppBarLayout.Behavior
                behavior?.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                    override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                        return false
                    }
                })
            }
        }

        // Listeners
        // Start 'Connection Power state' listener on android 8 or higher
        PowerConnectionService.start(ctx)

        presenter.listenAppSettingsChanged()
    }

    override fun onAppSettingsChanged(model: AppSettingsModel) {
        if (!shouldShowAdsRemovalFeature()) {
            // Update UI
            menuItemAd?.isVisible = false
            navItemAd?.isVisible = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Close drawer
        closeDrawer()

        when (item.itemId) {
            /*  R.id.nav_info -> {
                  presenter.delayBeforeDoing(250) {
                      openActivity(InfoActivity::class.java)
                  }
              }*/
//            R.id.nav_remove_ads -> {
//                removeAds()
//            }
//            R.id.nav_rate_app -> {
//                presenter.delayBeforeDoing(250) {
//                    showRateDialog()
//                }
//            }
//            R.id.nav_share_app -> {
//                CommonUtil.shareText(ctx, com.procharger.fastprocharrging.quickcharge.common.Constants.LINK_APP_ON_STORE)
//            }
//            R.id.nav_policy -> {
//                CommonUtil.openBrowser(ctx, com.procharger.fastprocharrging.quickcharge.common.Constants.LINK_POLICY)
//            }
//            R.id.nav_check_for_update -> {
//                CommonUtil.openAppInPlayStore(ctx)
//            }
        }
        return false
    }

    private fun parseIntent(intent: Intent?) {
        intent?.extras?.run {
            if (containsKey(com.procharger.fastprocharrging.quickcharge.common.Constants.KEY_ACTION)) {
                when (getString(com.procharger.fastprocharrging.quickcharge.common.Constants.KEY_ACTION)) {
                    com.procharger.fastprocharrging.quickcharge.common.Constants.ACTION_OPTIMIZE -> {
                        forceOptimize = true
                    }
                }
            }
        }
    }

    private fun closeDrawer() {
        drawerMain.closeDrawer(GravityCompat.START)
    }

    private fun removeAds() {
        showAdsRemovingDialog {
            presenter.removeAds(self)
        }
    }
    private fun showRateDialog() {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(R.color.transparent)
            setContentView(R.layout.dialog_rate_app)
            setCancelable(true)

            val ltvStar = findViewById<LottieAnimationView>(R.id.ltv_star)
            val btnNotNow = findViewById<TextView>(R.id.btn_not_now)
            val btnRate = findViewById<TextView>(R.id.btn_rate)
            val ckbNotShow = findViewById<CheckBox>(R.id.ckb_dont_show_again)

            ckbNotShow.visibility = View.GONE
//            if (appSettingsModel.dontShowRateDialogAgain) {
//                ckbNotShow.visibility = View.GONE
//            } else {
//                ckbNotShow.visibility = View.VISIBLE
//            }

            btnNotNow.setOnSafeClickListener {
                // Dismiss dialog
                ltvStar.cancelAnimation()
                dismiss()
            }

            btnRate.setOnSafeClickListener {
                // Dismiss dialog
                ltvStar.cancelAnimation()
                dismiss()

                // Open app on Play Store
                CommonUtil.openAppInPlayStore(ctx)

                // Save rate button clicked times
                appSettingsModel.run {
                    if (clickedRateButtonTimes < 2) {
                        clickedRateButtonTimes += 1
                        CommonUtil.saveAppSettingsModel(ctx, this)
                    }
                }
            }

            ckbNotShow.setOnCheckedChangeListener { _, isChecked ->
                // Save user option
                appSettingsModel.run {
                    dontShowRateDialogAgain = isChecked
                    CommonUtil.saveAppSettingsModel(ctx, this)
                }

                // Dismiss dialog
                ltvStar.cancelAnimation()
                dismiss()
            }

            setOnCancelListener {
                ltvStar.cancelAnimation()
            }

            if (!isShowing) {
                show()
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }
}