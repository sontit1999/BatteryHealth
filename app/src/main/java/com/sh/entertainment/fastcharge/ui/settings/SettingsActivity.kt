package com.sh.entertainment.fastcharge.ui.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaPlayer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.checkbox.MaterialCheckBox
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.NumberUtil
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import com.sh.entertainment.fastcharge.ui.base.BaseActivity


class SettingsActivity : BaseActivity<SettingsView, SettingsPresenterImp>(), SettingsView {

    companion object {
        private const val RC_DRAW_OVERLAY = 256
//        private const val RC_DRAW_OVERLAY = 257
    }

    private val chbBluetooth by lazy { findViewById<MaterialCheckBox>(R.id.chb_bluetooth) }
    private val chbScreenRotation by lazy { findViewById<MaterialCheckBox>(R.id.chb_screen_rotation) }
    private val chbRam by lazy { findViewById<MaterialCheckBox>(R.id.chb_ram) }
    private val chbSync by lazy { findViewById<MaterialCheckBox>(R.id.chb_sync) }
    private val chbScreenTimeOut by lazy { findViewById<MaterialCheckBox>(R.id.chb_screen_timeout) }
    private val chbWifi by lazy { findViewById<MaterialCheckBox>(R.id.chb_wifi) }
    private val lblWifi by lazy { findViewById<TextView>(R.id.lbl_disable_wifi) }
    //private val imgHelp by lazy { findViewById<ImageView>(R.id.img_help) }
    private val chbLaunchApp by lazy { findViewById<MaterialCheckBox>(R.id.chb_launch_app) }
    private val chbExitApp by lazy { findViewById<MaterialCheckBox>(R.id.chb_exit_app) }
    private val chbRestoreState by lazy { findViewById<MaterialCheckBox>(R.id.chb_restore_state) }
    private val imgPlaySound by lazy { findViewById<ImageView>(R.id.img_play_sound) }
    private val chbPlaySound by lazy { findViewById<SwitchCompat>(R.id.chb_play_sound) }
    private val chbDontDisturb by lazy { findViewById<MaterialCheckBox>(R.id.chb_dont_disturb) }
    private val lblFromLabel by lazy { findViewById<TextView>(R.id.lbl_from_label) }
    private val lblFromValue by lazy { findViewById<TextView>(R.id.lbl_from_value) }
    private val lblToLabel by lazy { findViewById<TextView>(R.id.lbl_to_label) }
    private val lblToValue by lazy { findViewById<TextView>(R.id.lbl_to_value) }
    private val lblLine by lazy { findViewById<View>(R.id.vw_play_sound) }
    private val txtOptionSound by lazy { findViewById<TextView>(R.id.lbl_dont_disturb) }
    private val cstInfoSetTime by lazy {
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(
            R.id.info_time_notification
        )
    }
    private val rbAuto by lazy { findViewById<SwitchCompat>(R.id.chb_auto) }
    private val cstAuto by lazy { findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.info_automation) }

    private var menuItemAd: MenuItem? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (shouldShowAdsRemovalFeature()) {
            val menuItem = menu?.findItem(R.id.menu_remove_ads)
            val rootView = menuItem?.actionView as? FrameLayout

            rootView?.setOnSafeClickListener {
                onOptionsItemSelected(menuItem)
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (shouldShowAdsRemovalFeature()) {
            menuInflater.inflate(R.menu.menu_settings, menu)
            menuItemAd = menu?.findItem(R.id.menu_remove_ads)
        }

        handleLoadInter()
        CommonUtil.saveIsAuto(this, canDrawOverlay())

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_remove_ads -> {
                showAdsRemovingDialog {
                    presenter.removeAds(self)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_DRAW_OVERLAY && canDrawOverlay()) {
            if (isXiaomiDevice) {
                // Update UI
                updateHelpIcon()
            } else {
               // imgHelp.gone()
            }
            CommonUtil.saveIsAuto(this, true)
            // Set result for the caller
            setResult(RESULT_OK)
        } else {
            rbAuto.isChecked = false
            updateAutoUI()
            CommonUtil.saveIsAuto(this, false)

            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        // Save app settings
        saveAppSettings()

        // Release media player
        releasePlayer()

        super.onDestroy()
    }

    override fun initView(): SettingsView {
        return this
    }

    override fun initPresenter(): SettingsPresenterImp {
        return SettingsPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_settings
    }

    override fun onBackPressed() {
        showInter()
    }

    override fun initWidgets() {
        // Init toolbar

        showTitle(R.string.settings)
        chbPlaySound.isChecked
        rbAuto.isChecked = CommonUtil.getIsAuto(this)
        hideShowSoundPlayingUI()
        hideToolbarBase()
        enableHomeAsUp {
            showInter()
        }
        updateAutoUI()
        // Listeners
        // Show wifi option if it's android api 28 or lower
        // Because of starting with Build.VERSION_CODES#Q, applications are not allowed to enable/disable Wi-Fi.
        if (!PermissionUtil.isApi29orHigher()) {
            chbWifi.visible()
            lblWifi.visible()
        }

        /*if (PermissionUtil.isApi23orHigher()) {
            if (isXiaomiDevice) {
                imgHelp.visible()
                if (canDrawOverlay()) {
                    updateHelpIcon()
                }
            } else {
                if (!canDrawOverlay()) {
                    imgHelp.visible()
                }
            }
            imgHelp.setOnSafeClickListener {
                if (canDrawOverlay()) {
                    if (isXiaomiDevice) {
                        showOtherPermissionsDescXiaomiDialog()
                    }
                } else {
                    showDrawOverlayPermissionDescDialog(self, RC_DRAW_OVERLAY)
                }
            }
        }*/

        imgPlaySound.setOnSafeClickListener {
            playBatteryFullSound()
        }

        chbPlaySound.setOnCheckedChangeListener { _, _ ->
            updateSoundPlayingUI()
            hideShowSoundPlayingUI()
        }

        chbDontDisturb.setOnCheckedChangeListener { _, _ ->
            updateSoundPlayingUI()
        }

        lblFromLabel.setOnSafeClickListener {
            showTimePicker(lblFromValue)
        }

        lblToLabel.setOnSafeClickListener {
            showTimePicker(lblToValue)
        }
        rbAuto.setOnCheckedChangeListener { _, _ ->
            CommonUtil.saveIsAuto(this, rbAuto.isChecked)
            updateAutoUI()
            // hideShowSoundPlayingUI();
            if (!canDrawOverlay() && rbAuto.isChecked) {
                showDrawOverlayPermissionDescDialog(onOkListener = {
                    requestDrawOverlayPermission(self, RC_DRAW_OVERLAY)
                }, onCancelListener = {
                    rbAuto.isChecked = false
                    updateAutoUI()
                })
            }
        }
        /*   if (PermissionUtil.isApi23orHigher()) {
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
        presenter.listenAppSettingsChanged()

        // Fill app settings
        appSettingsModel.run {
            chbBluetooth.isChecked = isTurnOffBluetooth
            chbRam.isChecked = isClearRam
            chbSync.isChecked = isTurnOffAutoSync
            chbWifi.isChecked = isTurnOffWifi
            chbScreenRotation.isChecked = isTurnOffScreenRotation
            chbScreenTimeOut.isChecked = isReduceScreenTimeOut

            chbLaunchApp.isChecked = isLaunchAppWhenPlugged
            chbExitApp.isChecked = isExitAppWhenUnplugged
            chbRestoreState.isChecked = isRestoreStateWhenUnplugged
            chbPlaySound.isChecked = isPlaySoundWhenBatteryFull
            chbDontDisturb.isChecked = dontPlaySoundWhile

            lblFromValue.text = String.format(
                getString(R.string._colon_),
                NumberUtil.getTwoDigitsNumber(dontPlaySoundFromHour),
                NumberUtil.getTwoDigitsNumber(dontPlaySoundFromMin)
            )
            lblToValue.text = String.format(
                getString(R.string._colon_),
                NumberUtil.getTwoDigitsNumber(dontPlaySoundToHour),
                NumberUtil.getTwoDigitsNumber(dontPlaySoundToMin)
            )
        }
    }

    private fun hideShowSoundPlayingUI() {
        if (chbPlaySound.isChecked) {
            /*lblLine.visible()
            chbDontDisturb.visible()
            txtOptionSound.visible()
            lblFromLabel.visible()
            lblToLabel.visible()
            lblFromValue.visible()
            lblToValue.visible()*/
            cstInfoSetTime.visible()
        } else {
            /*lblFromValue.gone()
            lblToValue.gone()
            chbDontDisturb.gone()
            txtOptionSound.gone()
            lblLine.gone();
            lblFromLabel.gone()
            lblToLabel.gone()*/
            cstInfoSetTime.gone()
        }
    }

    private fun updateAutoUI() {
        if (rbAuto.isChecked) {
            cstAuto.visible()
        } else {
            cstAuto.gone()
        }
    }

    override fun onAppSettingsChanged(model: AppSettingsModel) {
        if (!shouldShowAdsRemovalFeature()) {
            // Update UI
            menuItemAd?.isVisible = false
        }
    }

    private fun saveAppSettings() {
        ctx.appSettingsModel.apply {
            isClearRam = chbRam.isChecked
            isTurnOffAutoSync = chbSync.isChecked
            isTurnOffBluetooth = chbBluetooth.isChecked
            isTurnOffWifi = chbWifi.isChecked
            isTurnOffScreenRotation = chbScreenRotation.isChecked
            isReduceScreenTimeOut = chbScreenTimeOut.isChecked
            isLaunchAppWhenPlugged = chbLaunchApp.isChecked
            isExitAppWhenUnplugged = chbExitApp.isChecked
            isRestoreStateWhenUnplugged = chbRestoreState.isChecked
            isPlaySoundWhenBatteryFull = chbPlaySound.isChecked
            dontPlaySoundWhile = chbDontDisturb.isChecked
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
    }

    private fun updateSoundPlayingUI() {
        if (chbPlaySound.isChecked) {
            chbDontDisturb.isEnabled = true

            if (chbDontDisturb.isChecked) {
                lblFromLabel.isEnabled = true
                lblToLabel.isEnabled = true

                setSoundPlayingUI(
                    lblFromLabel, R.color.black, R.color.textColorPrimary,
                    lblFromValue, R.color.colorPrimaryDark
                )
                setSoundPlayingUI(
                    lblToLabel, R.color.black, R.color.textColorPrimary,
                    lblToValue, R.color.colorPrimaryDark
                )
            } else {
                lblFromLabel.isEnabled = false
                lblToLabel.isEnabled = false

                setSoundPlayingUI(
                    lblFromLabel, R.color.grey, R.color.grey, lblFromValue, R.color.grey
                )
                setSoundPlayingUI(
                    lblToLabel, R.color.grey, R.color.grey, lblToValue, R.color.grey
                )
            }
        } else {
            chbDontDisturb.isEnabled = false
            lblFromLabel.isEnabled = false
            lblToLabel.isEnabled = false

            setSoundPlayingUI(
                lblFromLabel, R.color.grey, R.color.grey, lblFromValue, R.color.grey
            )
            setSoundPlayingUI(
                lblToLabel, R.color.grey, R.color.grey, lblToValue, R.color.grey
            )
        }
    }

    private fun setSoundPlayingUI(
        lblLabel: TextView, labelDrawableColor: Int,
        labelTextColor: Int, lblValue: TextView, valueTextColor: Int
    ) {
        presenter.delayBeforeDoing(100) {
            for (drawable in lblLabel.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter = if (PermissionUtil.isApi29orHigher()) {
                        BlendModeColorFilter(
                            ContextCompat.getColor(self, labelDrawableColor), BlendMode.SRC_IN
                        )
                    } else {
                        PorterDuffColorFilter(
                            ContextCompat.getColor(ctx, labelDrawableColor), PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }
            lblLabel.textColor = labelTextColor
            lblValue.textColor = valueTextColor
        }
    }

    private fun showTimePicker(label: TextView) {
        var hour = 0
        var min = 0
        when (label) {
            lblFromValue -> {
                hour = appSettingsModel.dontPlaySoundFromHour
                min = appSettingsModel.dontPlaySoundFromMin
            }
            lblToValue -> {
                hour = appSettingsModel.dontPlaySoundToHour
                min = appSettingsModel.dontPlaySoundToMin
            }
        }
        TimePickerDialog(
            self, { _, selectedHour, selectedMin ->
                label.text = String.format(
                    getString(R.string._colon_),
                    NumberUtil.getTwoDigitsNumber(selectedHour),
                    NumberUtil.getTwoDigitsNumber(selectedMin)
                )

                // Assign to #appSettingsModel
                when (label) {
                    lblFromValue -> {
                        appSettingsModel.apply {
                            dontPlaySoundFromHour = selectedHour
                            dontPlaySoundFromMin = selectedMin
                        }
                    }
                    lblToValue -> {
                        appSettingsModel.apply {
                            dontPlaySoundToHour = selectedHour
                            dontPlaySoundToMin = selectedMin
                        }
                    }
                }
            }, hour, min, true
        ).show()
    }

    private fun playBatteryFullSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(ctx, R.raw.notification_battery_full)
        }
        mediaPlayer?.run {
            start()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            stop()
            reset()
            release()
            mediaPlayer = null
        }
    }

    private fun updateHelpIcon() {
       /* imgHelp.setColorFilter(
            ContextCompat.getColor(ctx, R.color.yellow),
            PorterDuff.Mode.SRC_IN
        )*/
    }

    private fun showInter() {
        Log.d("HaiHT", MyApplication.interstitialAd.toString())

        if (MyApplication.interstitialAd == null || !MyApplication.remoteConfigModel.is_inter_back_setting) {
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
                MyApplication.timeShowIntel = System.currentTimeMillis()
                finish()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                MyApplication.interstitialAd = null
                finish()
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