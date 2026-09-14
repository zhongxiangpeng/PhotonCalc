package com.photoncalc.app

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 轻量崩溃捕获:未捕获异常写入应用私有目录(最多保留 5 份),
 * 不联网、不依赖第三方 SDK;工具页"运行诊断"卡可查看与清除。
 */
object CrashReporter {

    private const val DIR_NAME = "crash"
    private const val MAX_REPORTS = 5
    private val timeFmt = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)

    fun install(context: Context) {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
                File(dir, "crash-${timeFmt.format(Date())}.txt").writeText(
                    buildString {
                        appendLine("time: ${Date()}")
                        appendLine("thread: ${thread.name}")
                        appendLine("version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                        appendLine("device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                        appendLine()
                        appendLine(Log.getStackTraceString(throwable))
                    },
                )
                dir.listFiles()
                    ?.sortedByDescending { it.name }
                    ?.drop(MAX_REPORTS)
                    ?.forEach { it.delete() }
            }
            prev?.uncaughtException(thread, throwable)
        }
    }

    fun latest(context: Context): File? =
        File(context.filesDir, DIR_NAME).listFiles()?.maxByOrNull { it.name }

    fun clear(context: Context) {
        File(context.filesDir, DIR_NAME).listFiles()?.forEach { it.delete() }
    }
}
