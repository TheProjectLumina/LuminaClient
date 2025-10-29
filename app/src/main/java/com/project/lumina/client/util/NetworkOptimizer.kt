package com.project.lumina.client.util

import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import java.net.InetAddress
import java.net.Socket
import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.net.Proxy

object NetworkOptimizer {
    private lateinit var connectivityManager: ConnectivityManager
    private const val TAG = "NetworkOptimizer"
    private var isInitialized = false
    private var optimizedSockets = mutableListOf<Socket>()

    fun init(context: Context): Boolean {
        if (isInitialized) {
            Log.d(TAG, "NetworkOptimizer already initialized")
            return true
        }

        connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        return try {
            if (hasRequiredPermissions(context)) {
                requestBestNetwork()
                isInitialized = true
                Log.d(TAG, "NetworkOptimizer initialized successfully")
                true
            } else {
                Log.e(TAG, "Missing required permissions for network optimization")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing network optimizer: ${e.message}")
            false
        }
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        
        val hasWriteSettings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true 
        }
        
        return hasWriteSettings
    }
    
    fun openWriteSettingsPermissionPage(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun requestBestNetwork() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Wi-Fi connected, binding process to Wi-Fi")
                    try {
                        connectivityManager.bindProcessToNetwork(network)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException: ${e.message}")
                    }
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "Wi-Fi lost, switching to mobile data")
                    switchToMobileData()
                }
            })
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when requesting network: ${e.message}")
        }
    }

    private fun switchToMobileData() {
        try {
            val mobileRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.requestNetwork(mobileRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Mobile data connected, binding process to mobile network")
                    try {
                        connectivityManager.bindProcessToNetwork(network)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException: ${e.message}")
                    }
                }
            })
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when switching to mobile data: ${e.message}")
        }
    }

    fun optimizeSocket(socket: Socket) {
        try {
            socket.keepAlive = true
            socket.tcpNoDelay = true
            socket.soTimeout = 30000
            socket.receiveBufferSize = 65536
            socket.sendBufferSize = 65536
            optimizedSockets.add(socket)
            Log.d(TAG, "Socket optimized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing socket: ${e.message}")
        }
    }

    fun createOptimizedSocket(): Socket {
        val socket = Socket()
        optimizeSocket(socket)
        return socket
    }

    fun setThreadPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)

            Thread.currentThread().priority = Thread.MAX_PRIORITY

            val currentThread = Thread.currentThread()
            Log.d(TAG, "Thread priority set - Current: ${currentThread.priority}, Process: ${Process.getThreadPriority(Process.myTid())}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting thread priority: ${e.message}")
        }
    }

    fun useFastDNS(): Boolean {
        return try {
            val googleDNS1 = InetAddress.getByName("8.8.8.8")
            val googleDNS2 = InetAddress.getByName("8.8.4.4")
            val cloudflareDNS = InetAddress.getByName("1.1.1.1")

            Log.d(TAG, "Fast DNS servers verified:")
            Log.d(TAG, "Google DNS 1: ${googleDNS1.hostAddress}")
            Log.d(TAG, "Google DNS 2: ${googleDNS2.hostAddress}")
            Log.d(TAG, "Cloudflare DNS: ${cloudflareDNS.hostAddress}")

            System.setProperty("java.net.preferIPv4Stack", "true")
            System.setProperty("networkaddress.cache.ttl", "60")
            System.setProperty("networkaddress.cache.negative.ttl", "10")

            Log.d(TAG, "DNS optimization settings applied")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring fast DNS: ${e.message}")
            false
        }
    }

    fun getOptimizedDNSServers(): List<String> {
        return listOf("8.8.8.8", "8.8.4.4", "1.1.1.1", "1.0.0.1")
    }

    fun cleanup() {
        try {
            optimizedSockets.forEach { socket ->
                if (!socket.isClosed) {
                    socket.close()
                }
            }
            optimizedSockets.clear()
            isInitialized = false
            Log.d(TAG, "NetworkOptimizer cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }

    fun getStatus(): String {
        return if (isInitialized) {
            "Initialized - ${optimizedSockets.size} optimized sockets"
        } else {
            "Not initialized"
        }
    }
}
