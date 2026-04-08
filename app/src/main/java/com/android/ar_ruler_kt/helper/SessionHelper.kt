package com.android.ar_ruler_kt.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import java.util.*

/**
 * @author：TianLong
 * @date：2022/6/27 20:59
 * @detail：ARCore Session Helper类
 */
object SessionHelper:HelperInterface{
    val featureSet: Set<Session.Feature> = setOf()
    lateinit var session: Session

    private val sessionConfig: Config by lazy {
        return@lazy Config(session).setFocusMode(Config.FocusMode.AUTO)
            .setUpdateMode(Config.UpdateMode.BLOCKING)
            .setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL)
    }
    fun initialize(context:Context):Boolean{
        return if (prepare(context)){
            try {
                session = Session(context,featureSet)

                val cameraConfigFilter = CameraConfigFilter(session)
                val list = session.getSupportedCameraConfigs(cameraConfigFilter)
                cameraConfigFilter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_60)
                cameraConfigFilter.depthSensorUsage = EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE)
                for (con in list){
                    Log.w(TAG,"id:${con.cameraId}   width:${con.imageSize.width}  height:${con.imageSize.height} upper:${con.fpsRange.upper }   lower${con.fpsRange.lower}")
                }
                session.cameraConfig=list.first()


                session.configure(session.config.apply {
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                    // Depth API is used if it is configured in Hello AR's settings.
                    depthMode =
                        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                            Config.DepthMode.AUTOMATIC
                        } else {
                            Config.DepthMode.DISABLED
                        }
                    depthMode =  Config.DepthMode.DISABLED
                    focusMode = Config.FocusMode.AUTO
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                })
            }catch (exception:Exception){
                var msg = when (exception) {
                    is UnavailableUserDeclinedInstallationException ->
                        "Please install Google Play Services for AR"
                    is UnavailableApkTooOldException -> "Please update ARCore"
                    is UnavailableSdkTooOldException -> "Please update this app"
                    is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                    is CameraNotAvailableException -> "Camera not available. Try restarting the app."
                    else -> "Failed to create AR session: $exception"
                }
                Log.e(TAG,msg)
                return false
            }

            true
        }else{
            false
        }
    }

    fun release(){
        session.close()
    }

    private fun prepare (context: Context):Boolean{
        when {
            (!CameraPermissionHelper.hasCameraPermission(context as Activity))-> {
                CameraPermissionHelper.requestCameraPermission(context)
                Log.w(TAG,"ARCore must have camera permission")
                return false
            }

            ArCoreApk.getInstance().requestInstall(context,true) == ArCoreApk.InstallStatus.INSTALL_REQUESTED ->{
                Log.w(TAG,"ARCore must install ArCoreApk")
                return false
            }
        }
        Log.w(TAG,"ARCore has prepared")
        return true
    }
}