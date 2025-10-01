package com.project.lumina.client.devtools

import android.content.Context
import android.os.Environment
import android.util.Log
import com.project.lumina.client.application.AppContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogcatService {
    
    private var logcatProcess: Process? = null
    private val logLines = mutableListOf<String>()

    private var logcatThread: Thread? = null
    private var isCapturing = false
    private var logFileWriter: BufferedWriter? = null
    private var logFile: File? = null
    private var isServiceRunning = false

    fun startLogcatService() {
        if (isServiceRunning) {
            Log.d("LogcatService", "Service already running")
            return
        }
        
        Log.d("LogcatService", "Starting Logcat Service")
        isServiceRunning = true
        createLogFile()
        startLogcatCapture()
    }

    fun stopLogcatService() {
        if (!isServiceRunning) {
            Log.d("LogcatService", "Service not running")
            return
        }
        
        Log.d("LogcatService", "Stopping Logcat Service")
        isServiceRunning = false
        stopLogcatCapture()
        closeLogFile()
    }

    fun isServiceRunning(): Boolean {
        return isServiceRunning
    }

    private fun isLogcatEnabled(): Boolean {
        return try {
            val sharedPreferences = AppContext.instance.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
            sharedPreferences.getBoolean("enableLogcatEnabled", false)
        } catch (e: Exception) {
            false
        }
    }

    fun checkAndStartIfEnabled() {
        if (isLogcatEnabled() && !isServiceRunning) {
            startLogcatService()
        } else if (!isLogcatEnabled() && isServiceRunning) {
            stopLogcatService()
        }
    }

    private fun startLogcatCapture() {
        try {
            stopLogcatCapture()

            Log.d("LogcatService", "Starting logcat capture for PID: ${android.os.Process.myPid()}")

            synchronized(logLines) {
                logLines.add("I/LogcatService: Logcat capture started")
                logLines.add("D/LogcatService: Process ID: ${android.os.Process.myPid()}")
                logLines.add("I/LogcatService: Terminal ready for logs...")
            }

            isCapturing = true
            logcatProcess = Runtime.getRuntime().exec(arrayOf(
                "logcat",
                "-v", "brief",
                "--pid=${android.os.Process.myPid()}"
            ))

            logcatThread = Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(logcatProcess!!.inputStream))
                    while (isCapturing) {
                        val line = reader.readLine()
                        if (line != null) {
                            synchronized(logLines) {
                                logLines.add(line)
                                if (logLines.size > 200) logLines.removeAt(0)
                            }
                            writeToLogFile(line)
                        } else {
                            break
                        }
                    }
                } catch (e: InterruptedException) {
                } catch (e: Exception) {
                    if (isCapturing) {
                        Log.e("LogcatService", "Error reading logcat: ${e.message}")
                    }
                }
            }
            logcatThread?.start()

        } catch (e: Exception) {
            Log.e("LogcatService", "Failed to start logcat: ${e.message}")
        }
    }

    private fun stopLogcatCapture() {
        try {
            isCapturing = false
            logcatProcess?.destroy()
            logcatThread?.interrupt()
            logcatThread?.join(1000)
            logcatProcess = null
            logcatThread = null
            Log.d("LogcatService", "Logcat capture stopped")
        } catch (e: Exception) {
            Log.e("LogcatService", "Failed to stop logcat: ${e.message}")
        }
    }

    private fun writeToLogFile(line: String) {
        try {
            logFileWriter?.apply {
                write("$line\n")
                flush()
            }
        } catch (e: Exception) {
            Log.e("LogcatService", "Failed to write to log file", e)
        }
    }

    private fun createLogFile() {
        try {
            val documentsDir = File(Environment.getExternalStorageDirectory(), "Documents")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "lumina_logcat_$timestamp.txt"

            logFile = File(documentsDir, fileName)
            logFileWriter = BufferedWriter(FileWriter(logFile, true))

            logFileWriter?.apply {
                write("=== Lumina Logcat Capture Started ===\n")
                write("Timestamp: ${Date()}\n")
                write("Process ID: ${android.os.Process.myPid()}\n")
                write("File: $fileName\n")
                write("=====================================\n\n")
                flush()
            }

            Log.d("LogcatService", "Log file created: ${logFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e("LogcatService", "Failed to create log file", e)
        }
    }

    private fun closeLogFile() {
        try {
            logFileWriter?.apply {
                write("\n=== Lumina Logcat Capture Ended ===\n")
                write("Timestamp: ${Date()}\n")
                write("===================================\n")
                flush()
                close()
            }
            logFileWriter = null

            logFile?.let { file ->
                Log.d("LogcatService", "Log file saved: ${file.absolutePath} (${file.length()} bytes)")
            }
            logFile = null
        } catch (e: Exception) {
            Log.e("LogcatService", "Failed to close log file", e)
        }
    }
}