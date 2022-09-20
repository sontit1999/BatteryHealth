package com.procharger.fastprocharrging.quickcharge.data.interactor

import android.app.ActivityManager
import android.content.Context
import android.provider.Settings
import com.procharger.fastprocharrging.quickcharge.common.extension.*
import com.procharger.fastprocharrging.quickcharge.common.util.CommonUtil

private const val SCREEN_TIMEOUT = 15000

class BoInteractor(private val ctx: Context) : BaseInteractor() {

    fun optimise(isCharging: Boolean) {
        with(ctx.appSettingsModel) {
            // Disable bluetooth
            if (isTurnOffBluetooth) {
                toggleBluetooth(false)
            }

            // Turn off wifi
            if (isTurnOffWifi) {
                toggleWifi(false)
            }

            // Turn off auto-sync
            if (isTurnOffAutoSync) {
                toggleAutoSync(false)
            }

            // Turn off screen rotation
            if (isTurnOffScreenRotation) {
                toggleScreenRotation(0)
            }

            // Reduce screen timeout
            if (isCharging && isReduceScreenTimeOut) {
                setScreenTimeOut(true)
            }

            // Kill background apps
            if (isClearRam) {
                killBackgroundApps()
            }
        }
    }

    fun restoreState() {
        with(ctx.appSettingsModel) {
            // Turn on bluetooth
            if (didTurnOffBluetooth) {
                toggleBluetooth(true)
            }

            // Turn on wifi
            if (didTurnOffWifi) {
                toggleWifi(true)
            }

            // Turn on auto-sync
            if (didTurnOffAuToSync) {
                toggleAutoSync(true)
            }

            // Turn on screen rotation
            if (didTurnOffScreenRotation) {
                toggleScreenRotation(1)
            }

            // Set screen timeout
            if (didReduceScreenTimeOut) {
                setScreenTimeOut(false)
            }

            // Save app settings into shared preference
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
    }

    private fun killBackgroundApps() {
        try {
            val packages = ctx.getInstalledApps()
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            activityManager?.run {
                for (packageInfo in packages) {
                    killBackgroundProcesses(packageInfo.packageName)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun toggleBluetooth(enable: Boolean) {
        if (enable) {
            if (!ctx.isBluetoothEnabled) {
                // Turn on
                val result = ctx.toggleBluetooth(true)

                // Update app settings flag
                ctx.appSettingsModel.didTurnOffBluetooth = false
            }
        } else {
            if (ctx.isBluetoothEnabled) {
                // Turn off
                val result = ctx.toggleBluetooth(false)

                // Update app settings flag
                ctx.appSettingsModel.didTurnOffBluetooth = true
            }
        }
    }

    private fun toggleWifi(enable: Boolean) {
        if (enable) {
            if (!ctx.isWifiEnabled) {
                // Turn on
                ctx.toggleWifi(true)

                // Update app settings flag
                ctx.appSettingsModel.didTurnOffWifi = false
            }
        } else {
            if (ctx.isWifiEnabled) {
                // Turn off
                ctx.toggleWifi(false)

                // Update app settings flag
                ctx.appSettingsModel.didTurnOffWifi = true
            }
        }
    }

    private fun toggleAutoSync(enable: Boolean) {
        if (ctx.isAutoSyncEnabled) {
            if (!enable) {
                // Turn off
                ctx.toggleAutoSync(false)

                // Update app settings flag
                ctx.appSettingsModel.didTurnOffAuToSync = true
            }
        } else {
            if (enable) {
                // Turn on
                ctx.toggleAutoSync(true)

                // Update app settings flag
                ctx.appSettingsModel.didTurnOffAuToSync = false
            }
        }
    }

    private fun turnOnAutomaticBrightness() {
        if (ctx.canWriteSettings()) {
            Settings.System.putInt(
                ctx.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
        }
    }

    private fun toggleScreenRotation(value: Int) {
        if (ctx.canWriteSettings()) {
            if (ctx.isAutoRotationEnabled) {
                if (value == 0) {
                    // Turn off
                    ctx.toggleAutoRotation(0)

                    // Update app settings flag
                    ctx.appSettingsModel.didTurnOffScreenRotation = true
                }
            } else {
                if (value == 1) {
                    // Turn on
                    ctx.toggleAutoRotation(1)

                    // Update app settings flag
                    ctx.appSettingsModel.didTurnOffScreenRotation = false
                }
            }
        }
    }

    private fun setScreenTimeOut(isReducing: Boolean) {
        if (ctx.canWriteSettings()) {
            if (isReducing) {
                ctx.appSettingsModel.deviceScreenTimeOut =
                    Settings.System.getInt(ctx.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
                if (ctx.appSettingsModel.deviceScreenTimeOut > SCREEN_TIMEOUT) {
                    ctx.appSettingsModel.didReduceScreenTimeOut = Settings.System.putInt(
                        ctx.contentResolver,
                        Settings.System.SCREEN_OFF_TIMEOUT,
                        SCREEN_TIMEOUT
                    )
                }
            } else {
                if (ctx.appSettingsModel.deviceScreenTimeOut > 0) {
                    Settings.System.putInt(
                        ctx.contentResolver,
                        Settings.System.SCREEN_OFF_TIMEOUT,
                        ctx.appSettingsModel.deviceScreenTimeOut
                    )
                }
            }
        }
    }
}