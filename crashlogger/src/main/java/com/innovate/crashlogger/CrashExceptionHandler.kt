package com.innovate.crashlogger

import io.sentry.core.SentryClient
import io.sentry.core.SentryEvent
import io.sentry.core.SentryLevel

class CrashExceptionHandler(var client: SentryClient,var defaultHandler:Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        val event = SentryEvent(e)
        event.level = SentryLevel.DEBUG
        client.captureEvent(event)
        if(null!=defaultHandler) {
            defaultHandler.uncaughtException(t, e)
            Thread.setDefaultUncaughtExceptionHandler(defaultHandler)
        }
    }
}