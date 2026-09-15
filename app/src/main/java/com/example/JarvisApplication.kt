package com.example
 
import android.app.Application
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Custom Application class for J.A.R.V.I.S.
 * Implements a Global Crash Shield (Application-Level UncaughtExceptionHandler)
 * to intercept residual runtime exceptions, serialize diagnostic crash traces,
 * and prevent the disruptive "J.A.R.V.I.S. keeps stopping" system dialog from looping.
 */
class JarvisApplication : Application() {

    companion object {
        private const val TAG = "JarvisCrashShield"
        const val CRASH_LOG_FILE = "jarvis_last_crash.log"

        @Volatile
        private var instance: JarvisApplication? = null

        fun getInstance(): JarvisApplication? = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        installGlobalCrashShield()
    }

    private fun installGlobalCrashShield() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "================ CRITICAL SYSTEM INTERCEPT ================")
                Log.e(TAG, "Uncaught exception on thread [${thread.name}] (id: ${thread.id}): ${throwable.message}", throwable)

                // Persist crash log locally for developer and user diagnostics
                val crashFile = File(filesDir, CRASH_LOG_FILE)
                val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())

                FileWriter(crashFile, false).use { fileWriter ->
                    PrintWriter(fileWriter).use { printWriter ->
                        printWriter.println("=== J.A.R.V.I.S. CRASH SHIELD DIAGNOSTIC ===")
                        printWriter.println("Timestamp: $timeStamp")
                        printWriter.println("Thread: ${thread.name} (id: ${thread.id})")
                        printWriter.println("Exception: ${throwable.javaClass.name}")
                        printWriter.println("Message: ${throwable.message}")
                        printWriter.println("--- STACK TRACE ---")
                        throwable.printStackTrace(printWriter)
                        printWriter.println("===========================================")
                    }
                }

                // If exception is related to Foreground Service Start Not Allowed or SecurityException on Android 14/15,
                // log and suppress hard crash to safeguard the user experience
                val isServiceStartException = throwable.javaClass.simpleName.contains("ForegroundServiceStartNotAllowedException") ||
                        (throwable is SecurityException && throwable.message?.contains("foreground", ignoreCase = true) == true)

                if (isServiceStartException) {
                    Log.w(TAG, "Suppressed background foreground service race exception: ${throwable.message}")
                    return@setDefaultUncaughtExceptionHandler
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error writing crash log to internal storage", e)
            }

            // Delegate to default handler if unrecoverable critical error
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.i(TAG, "Global Crash Shield installed successfully")
    }
}
