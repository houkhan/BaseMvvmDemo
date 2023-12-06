package com.hzsoft.lib.base.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.hzsoft.lib.base.BaseApp

object ProcessUtils {

    private const val TAG = "ProcessUtils"
    private const val ACTIVITY_THREAD = "android.app.ActivityThread"
    private const val CURRENT_ACTIVITY_THREAD = "currentActivityThread"
    private const val GET_PROCESS_NAME = "getProcessName"

    private val context: Context
        get() = BaseApp.getContext().applicationContext

    val pid: Int
        get() = Process.myPid()

    /**
     * 是否为主进程
     */
    var isMainProcess: Boolean? = null
        get() {
            if (field == null) {
                val context = context
                val currentProcessName = processName
                field = TextUtils.equals(currentProcessName, context.packageName)
                LogUtils.dTag(TAG, "isMainProcess $field processName: $currentProcessName")
            }
            return field
        }
        private set

    private var currentProcessName: String? = null

    val processName: String?
        get() {
            try {
                if (!TextUtils.isEmpty(currentProcessName)) {
                    return currentProcessName
                }
                try {
                    val activityThread: Any? =
                        ReflectUtils.invokeMethod(ACTIVITY_THREAD, CURRENT_ACTIVITY_THREAD)
                    currentProcessName =
                        ReflectUtils.invokeMethod(activityThread!!, GET_PROCESS_NAME) as String?
                    LogUtils.dTag(TAG, "getProcessName from ActivityThread: $currentProcessName")
                } catch (tr: Throwable) {
                    LogUtils.eTag(TAG, "getProcessName error!", tr)
                }
                if (currentProcessName == null) {
                    val context = context
                    val pid = Process.myPid()
                    val manager =
                        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    for (process in manager.runningAppProcesses) {
                        if (process.pid == pid) {
                            currentProcessName = process.processName
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtils.eTag(TAG, "getProcessName error", e)
            }
            return currentProcessName
        }
}