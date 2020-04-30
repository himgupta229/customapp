package com.innovate.crashlogger

import android.content.Context
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.core.SentryClient
import java.io.File


object CrashManager {

    fun init(context: Context) {
        createSentryClient(context)
    }

    fun createSentryClient(context: Context) {
        var sentryOptions = SentryAndroidOptions();
        sentryOptions?.setBeforeSend { event, any ->
            if (event.getExceptions() != null && !event.getExceptions()
                    .isEmpty() && event.getExceptions().get(0).getModule().contains("com.payu.")
            ) {

                // you can even look up at the the frames or just return the event at this point
                for (item in event.getExceptions().get(0).getStacktrace()
                    .getFrames()) {
                    if (item.module.contains("com.innovate.")) {
                        return@setBeforeSend event // send the event
                    }
                }
            }
            null // drop the event
        };
        sentryOptions?.dsn =
            "https://a7ccef28c9174ff6baf578798d614d48@o377051.ingest.sentry.io/5198595"
        setCachePath(context,sentryOptions)
        val sentryClient = SentryClient(sentryOptions, null)
        setupExceptionHandler(sentryClient)
    }

    private fun setCachePath(context: Context, options: SentryAndroidOptions) {
        val cacheDir = File(context.getCacheDir(), "PayUSentry")
        cacheDir.mkdirs()
        options.cacheDirPath = cacheDir.absolutePath
    }

    private fun setupExceptionHandler(client: SentryClient) {
        if (!(Thread.getDefaultUncaughtExceptionHandler() is CrashExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(CrashExceptionHandler(client))
        }
    }
}