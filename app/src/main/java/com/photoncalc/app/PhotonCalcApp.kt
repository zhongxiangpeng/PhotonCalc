package com.photoncalc.app

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.util.Log
import android.view.Choreographer

class PhotonCalcApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        if (BuildConfig.DEBUG) {
            // 主线程磁盘/网络误用与资源泄漏检测(仅调试构建,release 零开销)
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build(),
            )
            startJankWatch()
        }
    }

    /** 调试构建的掉帧监测:单帧超过约 10 个垂直同步周期记一条告警日志。 */
    private fun startJankWatch() {
        var last = 0L
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (last != 0L) {
                    val dtMs = (frameTimeNanos - last) / 1_000_000
                    if (dtMs > 700) Log.w("JankWatch", "主线程单帧 ${dtMs} ms,疑似卡顿")
                }
                last = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }
}
