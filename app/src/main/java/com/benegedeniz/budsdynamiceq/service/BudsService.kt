package com.benegedeniz.budsdynamiceq.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.benegedeniz.budsdynamiceq.R
import com.benegedeniz.budsdynamiceq.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BudsService : Service() {

    companion object {
        private const val TAG = "BudsService"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val transientNotificationFlow = MutableStateFlow<Pair<String, String>?>(null)

    private lateinit var notificationManagerHelper: NotificationManagerHelper
    private var notificationCoordinator: NotificationCoordinator? = null

    override fun onCreate() {
        super.onCreate()

        notificationManagerHelper = NotificationManagerHelper(this)
        notificationManagerHelper.createNotificationChannel()
        startForeground(
            1,
            notificationManagerHelper.buildNotification(
                titleText = getString(R.string.connecting_97),
                ruleNcText = getString(R.string.waiting_for_connection),
                hardwareNcText = "",
                lBatteryText = "",
                rBatteryText = "",
                isLWorn = false,
                isRWorn = false,
                isConnected = false,
                toggleButtonText = ""
            )
        )

        val budsController = ServiceLocator.provideBudsController(this)
        budsController.startAutoConnect()

        // Stop the service if the buds are neither connected nor connecting
        scope.launch {
            delay(2000)
            combine(
                budsController.isConnected,
                budsController.isConnecting
            ) { connected, connecting -> connected to connecting }
                .collect { (connected, connecting) ->
                    if (!connected && !connecting) {
                        Log.i(TAG, "Not connected and not connecting, stopping service.")
                        stopSelf()
                    }
                }
        }

        notificationCoordinator = NotificationCoordinator(
            context = this,
            scope = scope,
            budsController = budsController,
            transientNotificationFlow = transientNotificationFlow,
            notificationManagerHelper = notificationManagerHelper
        )
        notificationCoordinator!!.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "com.benegedeniz.budsdynamiceq.AUTO_CONNECT") {
            ServiceLocator.provideBudsController(this).startAutoConnect()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        notificationCoordinator?.stop()
        scope.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        getSystemService(NotificationManager::class.java).cancel(1)

        ServiceLocator.provideBudsController(this).disconnect(forget = false)
    }
}