package com.sh.entertainment.fastcharge.data.model

class AppSettingsModel : BaseModel() {
    // App settings fields
    var didRemoveAds = false
    var didCheckVipStatus = false
    var isLaunchAppWhenPlugged = true
    var isExitAppWhenUnplugged = true
    var isRestoreStateWhenUnplugged = true
    var isPlaySoundWhenBatteryFull = true
    var dontPlaySoundWhile = true
    var dontPlaySoundFromHour = 23
    var dontPlaySoundFromMin = 0
    var dontPlaySoundToHour = 7
    var dontPlaySoundToMin = 30
    var didShowOptimizationDescDialog = false
    var isFirstTimeAppOpened = true
    var appOpenedTimes = 0
    var batteryPercentage: Float? = null
    var clickedRateButtonTimes = 0
    var optimizedTimes = 0
    var dontShowRateDialogAgain = false
    var isShowSnailIcon = false

    // Battery optimization settings fields
    var isClearRam = true
    var isTurnOffBluetooth = true
    var isTurnOffWifi = false
    var isTurnOffAutoSync = false
    var isTurnOffScreenRotation = true
    var isReduceScreenTimeOut = true
    var didTurnOffBluetooth = false
    var didTurnOffWifi = false
    var didTurnOffAuToSync = false
    var didTurnOffScreenRotation = false
    var didReduceScreenTimeOut = false
    var deviceScreenTimeOut = 0

    fun isShowRateDialog(): Boolean {
        val optimizationSuccessTimesValid = optimizedTimes == 3 || optimizedTimes == 5 ||
                optimizedTimes == 7 || (optimizedTimes > 7 && (optimizedTimes - 7) % 3 == 0)
        return optimizationSuccessTimesValid && clickedRateButtonTimes < 2 && !dontShowRateDialogAgain
    }
}