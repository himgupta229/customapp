package com.innovate.crashlogger

import android.content.Context
import android.util.Log
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.core.*
import io.sentry.core.transport.ITransportGate
import java.io.File
import java.util.*


object CrashManager {

    fun init(context: Context) {
        createSentryClient(context)
    }

    fun createSentryClient(context: Context) {
        val sentryOptions = SentryAndroidOptions();
        sentryOptions.setSerializer(createSerializerInstance())
        sentryOptions.transportGate = createTransportInstance(context)
        sentryOptions.isDebug = true
        sentryOptions.setLogger(createLoggerInstance())
        sentryOptions?.setBeforeSend { event, any ->
            val exceptions = event.exceptions
            for (e in exceptions) {
                val frames = e.stacktrace.frames
                for (frame in frames) {
                    if (frame.module.contains("com.innovate")) {
                        return@setBeforeSend event
                    }
                }
            }
            null // drop the event
        };
        sentryOptions?.dsn =
                "https://a7ccef28c9174ff6baf578798d614d48@o377051.ingest.sentry.io/5198595"
        setCachePath(context, sentryOptions)
        sentryOptions.addEventProcessor(createEventProcessorInstance(context,sentryOptions))
        val sentryClient = SentryClient(sentryOptions, null)
        setupExceptionHandler(sentryClient)
    }

    private fun createTransportInstance(context: Context): ITransportGate {
        val classLoader = CrashManager::class.java.classLoader
        val className = "io.sentry.android.core.AndroidTransportGate"
        val customClass = classLoader?.loadClass(className)
        val constructors = customClass?.declaredConstructors
        Log.d("payu", Arrays.toString(constructors))
        val constructor = customClass?.getDeclaredConstructor(Context::class.java, ILogger::class.java)
        constructor?.isAccessible = true
        val logger = createLoggerInstance()
        val result = constructor?.newInstance(context, logger)
        return result as ITransportGate
    }

    private fun createEventProcessorInstance(context: Context,options: SentryAndroidOptions): EventProcessor {
        val classLoader = CrashManager::class.java.classLoader
        val className = "io.sentry.android.core.DefaultAndroidEventProcessor"
        val customClass = classLoader?.loadClass(className)
        val constructors = customClass?.declaredConstructors
        Log.d("payu", Arrays.toString(constructors))
        val constructor = customClass?.getDeclaredConstructor(Context::class.java, SentryOptions::class.java)
        constructor?.isAccessible = true
        val result = constructor?.newInstance(context, options)
        return result as EventProcessor
    }

    private fun setCachePath(context: Context, options: SentryAndroidOptions) {
        val cacheDir = File(context.getCacheDir(), "CustomSentry")
        cacheDir.mkdirs()
        options.cacheDirPath = cacheDir.absolutePath
    }

    private fun setupExceptionHandler(client: SentryClient) {
        var defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (!(Thread.getDefaultUncaughtExceptionHandler() is CrashExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(CrashExceptionHandler(client, defaultHandler))
        }
    }

    private fun createSerializerInstance(): ISerializer? {
        var classLoader = CrashManager::class.java.classLoader
        val className = "io.sentry.android.core.AndroidSerializer"
        val customClass = classLoader?.loadClass(className)
        val constructors = customClass?.declaredConstructors
        Log.d("payu", Arrays.toString(constructors))
        val constructor = customClass?.getDeclaredConstructor(ILogger::class.java)
        constructor?.isAccessible = true
        var logger = createLoggerInstance()
        val result = constructor?.newInstance(logger)
        return result as ISerializer
    }

    private fun createLoggerInstance(): ILogger {
        val className = "io.sentry.android.core.AndroidLogger"
        val customClass = CrashManager::class.java.classLoader?.loadClass(className)
        val constructors = customClass?.declaredConstructors
        Log.d("payu", Arrays.toString(constructors))
        val constructor = customClass?.getDeclaredConstructor()
        constructor?.isAccessible = true
        val result = constructor?.newInstance()
        return result as ILogger
    }
}