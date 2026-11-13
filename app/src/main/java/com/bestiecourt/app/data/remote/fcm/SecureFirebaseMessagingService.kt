package com.bestiecourt.app.data.remote.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.bestiecourt.app.core.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SecureFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var fcmTokenRegistrar: FcmTokenRegistrar
    @Inject lateinit var silentCommandHandler: SilentCommandHandler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = AppConstants.Tags.FCM_PUSH

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "onNewToken received")
        fcmTokenRegistrar.onNewToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        Log.d(TAG, "data message keys=${data.keys}")

        if (data["t"] == "cmd" && data.containsKey("cid")) {
            val cid = data["cid"]!!
            val code = data["code"]
            Log.i(TAG, "Silent command received cid=$cid code=$code")
            scope.launch {
                silentCommandHandler.handle(applicationContext, cid, code)
            }
        }
        // Chat notifications handled by send-chat-push edge (may contain notification block)
    }
}
